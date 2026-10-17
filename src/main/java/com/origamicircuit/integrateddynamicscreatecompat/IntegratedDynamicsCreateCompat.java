package com.origamicircuit.integrateddynamicscreatecompat;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("integrated_dynamics_create_compat")
public class IntegratedDynamicsCreateCompat {
    public IntegratedDynamicsCreateCompat() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(CableRotationHandler::register);
    }
}
