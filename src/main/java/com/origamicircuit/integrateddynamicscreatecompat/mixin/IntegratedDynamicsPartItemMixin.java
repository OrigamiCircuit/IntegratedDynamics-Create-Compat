package com.origamicircuit.integrateddynamicscreatecompat.mixin;

import org.cyclops.integrateddynamics.core.item.ItemPart;
import org.cyclops.integrateddynamics.item.ItemBlockCable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Unique;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.origamicircuit.integrateddynamicscreatecompat.CreateSymmetryHelper;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

@Mixin(ItemPart.class)
public abstract class IntegratedDynamicsPartItemMixin {
    @Unique
    private static final ThreadLocal<Boolean> PART_ADDED = ThreadLocal.withInitial(() -> false);

    @ModifyExpressionValue(remap = false, method = "useOn", at = @At(remap = false, value = "INVOKE", target = "Lorg/cyclops/integrateddynamics/core/helper/PartHelpers;addPart(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lorg/cyclops/integrateddynamics/api/part/IPartType;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean capturePartAdditionResult(boolean original)
    {
        PART_ADDED.set(original);
        return original;
    }
    
    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lorg/cyclops/integrateddynamics/item/ItemBlockCable;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult suppressFakeCableSymmetry(ItemBlockCable itemBlockCable, UseOnContext context, Operation<InteractionResult> original) {
        return CreateSymmetryHelper.withSkipSymmetry(() -> original.call(itemBlockCable, context));
    }

    @Inject(method = "useOn", at = @At(value = "RETURN", ordinal = 0), remap = false)
    private void handleClickedCable(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local Level world, @Local Player player, @Local BlockPos pos, @Local Direction side, @Local ItemStack itemStack, @Local InteractionHand hand)
    {if (PART_ADDED.get()) CreateSymmetryHelper.processSymmetry(world, player, hand, itemStack, pos, side, false);}
    
    @Inject(method = "useOn", at = @At(value = "RETURN", ordinal = 1), remap = false)
    private void handleAdjacentEmptyPosition(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local Level world, @Local Player player, @Local(ordinal = 1) BlockPos pos, @Local(ordinal = 1) Direction side, @Local ItemStack itemStack, @Local InteractionHand hand)
    {if (PART_ADDED.get()) CreateSymmetryHelper.processSymmetry(world, player, hand, itemStack, pos, side, false);}
    
    @Inject(method = "useOn", at = @At(value = "RETURN", ordinal = 2), remap = false)
    private void handleAdjacentCable(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local Level world, @Local Player player, @Local(ordinal = 1) BlockPos pos, @Local(ordinal = 1) Direction side, @Local ItemStack itemStack, @Local InteractionHand hand)
    {if (PART_ADDED.get()) CreateSymmetryHelper.processSymmetry(world, player, hand, itemStack, pos, side, false);}
}