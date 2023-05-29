package com.refinedmods.refinedstorage2.platform.common.util;

public final class MathHelper {
    private MathHelper() {
    }

    public static long clamp(final long value, final long min, final long max) {
        return Math.min(Math.max(value, min), max);
    }
}
