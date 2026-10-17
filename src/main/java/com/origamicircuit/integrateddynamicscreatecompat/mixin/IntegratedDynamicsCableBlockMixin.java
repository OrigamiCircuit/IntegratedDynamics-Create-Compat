package com.origamicircuit.integrateddynamicscreatecompat.mixin;

import org.cyclops.integrateddynamics.block.BlockCable;
import org.cyclops.integrateddynamics.core.block.BlockRayTraceResultComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.entity.player.Player;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.origamicircuit.integrateddynamicscreatecompat.CableDropSuppressionHelper;
import com.origamicircuit.integrateddynamicscreatecompat.CreateSymmetryHelper;

import net.minecraft.core.BlockPos;

@Mixin(BlockCable.class)
public abstract class IntegratedDynamicsCableBlockMixin {

    @WrapOperation(method = "onRemove", remap = false, at = @At(value = "INVOKE", target = "Lorg/cyclops/integrateddynamics/core/helper/CableHelpers;onCableRemoving(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZZ)Z", remap = false))
    private boolean suppressCableDrops(Level world, BlockPos pos, boolean dropMainElement, boolean saveState, Operation<Boolean> original, @Local(argsOnly = true) boolean isMoving) {
        if (isMoving) {
            return CableDropSuppressionHelper.withCableDropsSuppressed(() -> original.call(world, pos, dropMainElement, saveState));
        }
        return original.call(world, pos, dropMainElement, saveState);
    }
    @Inject(method = "onDestroyedByPlayer", remap = false, at = @At("RETURN"))
    private void createSymmetry$onDestroyedByPlayer(BlockState state, Level world, BlockPos pos, Player player, boolean willHarvest, FluidState fluid, CallbackInfoReturnable<Boolean> cir, @Local BlockRayTraceResultComponent rayTraceResult) {
        if (rayTraceResult != null) {
            CreateSymmetryHelper.processSymmetry(world, player, null, null, pos, rayTraceResult.getComponent().getRaytraceDirection(), true);
        }
    }
}