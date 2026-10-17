package com.origamicircuit.integrateddynamicscreatecompat.mixin;

import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.cyclops.integrateddynamics.RegistryEntries;

@Mixin(Contraption.class)
public abstract class CreateContraptionsMixin {
    @WrapWithCondition(method = "removeBlocksFromWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlockEntity(Lnet/minecraft/core/BlockPos;)V"))
    private boolean skipCableEntityRemoval(Level world, BlockPos pos) {
        return !world.getBlockState(pos).is(RegistryEntries.BLOCK_CABLE);
    }
    @Inject(method = "addBlocksToWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;markAndNotifyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/state/BlockState;II)V", remap = false), remap = false)
    private void updateCableOnPlacement(Level world, StructureTransform transform, CallbackInfo ci, @Local(ordinal = 0) BlockPos pos) {
        if (!world.getBlockState(pos).is(RegistryEntries.BLOCK_CABLE)) return;
        CableHelpers.updateConnections(world, pos, null);
        CableHelpers.onCableAdded(world, pos);
    }
}
