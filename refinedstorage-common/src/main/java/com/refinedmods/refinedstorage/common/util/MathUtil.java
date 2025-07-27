package com.refinedmods.refinedstorage.common.util;

public final class MathUtil {
    private MathUtil() {
    }

    public static long clamp(final long value, final long min, final long max) {
        return Math.min(Math.max(value, min), max);
    }

    public static int darkenARGB(final int argb, final double percentage) {
        final int alpha = (argb >> 24) & 0xFF;
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;

        red = (int) Math.max(0, red * (1 - percentage));
        green = (int) Math.max(0, green * (1 - percentage));
        blue = (int) Math.max(0, blue * (1 - percentage));

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}
