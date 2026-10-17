package com.origamicircuit.integrateddynamicscreatecompat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.core.helper.PartHelpers;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryEffectPacket;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.content.equipment.symmetryWand.mirror.SymmetryMirror;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.minecraft.world.level.block.Blocks;
import java.util.Optional;
import java.util.List;
import java.util.Map;
import org.cyclops.integrateddynamics.RegistryEntries;

public final class CreateSymmetryHelper {
    private static final ThreadLocal<Boolean> SkipSymmetry = ThreadLocal.withInitial(() -> false);

    public static <T> T withSkipSymmetry(Supplier<T> action) {
        boolean previous = SkipSymmetry.get();
        SkipSymmetry.set(true);

        try {
            return action.get();
        } finally {
            SkipSymmetry.set(previous);
        }
    }

    public static void withSkipSymmetry(Runnable action) {
        boolean previous = SkipSymmetry.get();
        SkipSymmetry.set(true);
        try {
            action.run();
        } finally {
            SkipSymmetry.set(previous);
        }
    }

    public static void processSymmetry(Level world, Player player, InteractionHand hand, ItemStack placedStack, BlockPos pos, Direction side, boolean removing) {
        if (world.isClientSide) return;
        if (SkipSymmetry.get()) return;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            if (AllItems.WAND_OF_SYMMETRY.isIn(inventory.getItem(i)))
                processWandSymmetry(world, player, hand, placedStack, pos, side, removing, inventory, inventory.getItem(i));
        }
    }
    
    static void processWandSymmetry(Level world, Player player, InteractionHand hand, ItemStack placedStack, BlockPos pos, Direction side, boolean removing, Inventory inventory, ItemStack wand) {
        SymmetryMirror mirror = SymmetryWandItem.getMirror(wand);
 
        if (!SymmetryWandItem.isEnabled(wand)) return;
 
        Vec3 mirrorPos = mirror.getPosition();
        if (mirrorPos.distanceTo(Vec3.atLowerCornerOf(pos)) > AllConfigs.server().equipment.maxSymmetryWandRange.get()) return;
 
        ItemStack source = null;
        if (!removing) {
            source = findFuzzyStack(player, hand, placedStack).orElse(null);
            if (source == null) return;
        }
 
        BlockState probe = side == null
                ? Blocks.DIRT.defaultBlockState()
                : Blocks.PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, side);
 
        BlockPos to = BlockPos.containing(mirrorPos);
        List<BlockPos> targets = new ArrayList<>();
        targets.add(pos);
 
        Map<BlockPos, BlockState> blockSet = new HashMap<>();
        blockSet.put(pos, probe);
        mirror.process(blockSet);
 
        for (Map.Entry<BlockPos, BlockState> entry : blockSet.entrySet()) {
            BlockPos targetPos = entry.getKey();
            Direction targetSide = entry.getValue().getOptionalValue(PistonBaseBlock.FACING).orElse(Direction.NORTH);
 
            if (targetPos.equals(pos)) continue;
 
            if (!player.isCreative()) {
                BlockState existing = world.getBlockState(targetPos);
                if (!existing.canBeReplaced() && !existing.is(RegistryEntries.BLOCK_CABLE)) continue;
                if (existing.getDestroySpeed(world, targetPos) == -1) continue;
            }
 
            if (removing) {
                if (!world.getBlockState(targetPos).is(RegistryEntries.BLOCK_CABLE)) continue;

                if (side == null) {
                    if (CableHelpers.getCableFakeable(world, targetPos, null).map(fakeable -> fakeable.isRealCable()).orElse(false)) targets.add(targetPos);
                    CableHelpers.removeCable(world, targetPos, player);
                } else {
                    if (PartHelpers.getPartContainer(world, targetPos, targetSide).map(partContainer -> partContainer.hasPart(targetSide)).orElse(false)) targets.add(targetPos);
                    PartHelpers.removePart(world, targetPos, targetSide, player, true, true, false);
                }
            } else {
                if (source.isEmpty()) {
                    source = findFuzzyStack(player, hand, placedStack).orElse(null);
                    if (source == null) break;
                }

                final ItemStack useStack = source;
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(targetPos), targetSide, targetPos, false);
                UseOnContext context = new UseOnContext(world, player, hand, useStack, hit);
            
                CreateSymmetryHelper.withSkipSymmetry(() -> {
                    if (useStack.getItem() != RegistryEntries.ITEM_CABLE && world.getBlockState(targetPos).getBlock() != RegistryEntries.BLOCK_CABLE) {
                        RegistryEntries.ITEM_CABLE.useOn(new UseOnContext(world, player, hand, new ItemStack(RegistryEntries.ITEM_CABLE), hit));
                        CableHelpers.onCableRemoving(world, targetPos, false, false);
                        CableHelpers.getCableFakeable(world, targetPos, null).ifPresent(fakeable -> fakeable.setRealCable(false));
                        CableHelpers.onCableRemoved(world, targetPos, CableHelpers.ALL_SIDES);
                    }
                    if (useStack.useOn(context).consumesAction())
                        targets.add(targetPos);
                });
            }
        }
 
        AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SymmetryEffectPacket(to, targets));
    }
 
    private static Optional<ItemStack> findFuzzyStack(Player player, InteractionHand hand, ItemStack target) {
        ItemStack held = player.getItemInHand(hand);
        if (isFuzzyMatch(held, target))
            return Optional.of(held);
 
        InteractionHand otherHand = otherHand(hand);
        ItemStack otherHeld = player.getItemInHand(otherHand);
        if (isFuzzyMatch(otherHeld, target))
            return Optional.of(otherHeld);
 
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (isFuzzyMatch(stack, target))
                return Optional.of(stack);
        }
 
        return Optional.empty();
    }
 
    private static InteractionHand otherHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
 
    private static boolean isFuzzyMatch(ItemStack stack, ItemStack target) {
        return !stack.isEmpty() && ItemStack.isSameItem(stack, target);
    }

}
