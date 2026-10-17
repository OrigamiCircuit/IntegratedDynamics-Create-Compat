
package com.origamicircuit.integrateddynamicscreatecompat.mixin;

import com.simibubi.create.content.equipment.symmetryWand.SymmetryHandler;

import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;

import org.cyclops.integrateddynamics.block.BlockCable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import net.minecraftforge.event.level.BlockEvent;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SymmetryHandler.class, remap = false)
public abstract class CreateSymmetryHandlerMixin {
    @Inject(method = "onBlockPlaced", at = @At("HEAD"), cancellable = true)
    private static void onBlockPlaced(EntityPlaceEvent event, CallbackInfo ci) {
        if (event.getPlacedBlock().getBlock() instanceof BlockCable) ci.cancel();
    }
    @Inject(method = "onBlockDestroyed", at = @At("HEAD"), cancellable = true)
    private static void onBlockDestroyed(BlockEvent.BreakEvent event, CallbackInfo ci) {
        if (event.getState().getBlock() instanceof BlockCable) ci.cancel();
    }
}
