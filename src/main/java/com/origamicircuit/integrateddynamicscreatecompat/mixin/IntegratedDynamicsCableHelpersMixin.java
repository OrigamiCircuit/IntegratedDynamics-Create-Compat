package com.origamicircuit.integrateddynamicscreatecompat.mixin;

import org.cyclops.integrateddynamics.api.network.INetworkElementProvider;
import org.cyclops.integrateddynamics.core.helper.CableHelpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.origamicircuit.integrateddynamicscreatecompat.CableDropSuppressionHelper;
import net.minecraftforge.common.util.LazyOptional;

@Mixin(CableHelpers.class)
public abstract class IntegratedDynamicsCableHelpersMixin {
    @ModifyExpressionValue(method = "onCableRemoving", remap = false, at = @At(value = "INVOKE", target = "Lorg/cyclops/integrateddynamics/core/helper/NetworkHelpers;getNetworkElementProvider(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;", remap = false))
    private static LazyOptional<INetworkElementProvider> suppressCableDrops(LazyOptional<INetworkElementProvider> original) {
        return CableDropSuppressionHelper.SUPPRESS_CABLE_DROPS.get() ? LazyOptional.empty() : original;
    }
}