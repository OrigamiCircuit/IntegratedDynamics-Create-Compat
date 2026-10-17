package com.origamicircuit.integrateddynamicscreatecompat;

import java.util.function.Supplier;

public final class CableDropSuppressionHelper {
    public static final ThreadLocal<Boolean> SUPPRESS_CABLE_DROPS = ThreadLocal.withInitial(() -> false);
    public static <T> T withCableDropsSuppressed(Supplier<T> action) {
        boolean previous = SUPPRESS_CABLE_DROPS.get();
        SUPPRESS_CABLE_DROPS.set(true);
        try {
            return action.get();
        } finally {
            SUPPRESS_CABLE_DROPS.set(previous);
        }
    }

    public static void withCableDropSuppressed(Runnable action) {
        boolean previous = SUPPRESS_CABLE_DROPS.get();
        SUPPRESS_CABLE_DROPS.set(true);
        try {
            action.run();
        } finally {
            SUPPRESS_CABLE_DROPS.set(previous);
        }
    }
}
