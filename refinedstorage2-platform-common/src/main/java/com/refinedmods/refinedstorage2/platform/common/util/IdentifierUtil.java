package com.refinedmods.refinedstorage2.platform.common.util;

import com.refinedmods.refinedstorage2.platform.api.support.AmountFormatting;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class IdentifierUtil {
    public static final String MOD_ID = "refinedstorage2";

    public static final MutableComponent YES = Component.translatable("gui.yes");
    public static final MutableComponent NO = Component.translatable("gui.no");

    private IdentifierUtil() {
    }

    public static ResourceLocation createIdentifier(final String value) {
        return new ResourceLocation(MOD_ID, value);
    }

    public static String createTranslationKey(final String category, final String value) {
        return String.format("%s.%s.%s", category, MOD_ID, value);
    }

    public static MutableComponent createTranslation(final String category, final String value) {
        return Component.translatable(createTranslationKey(category, value));
    }

    public static MutableComponent createTranslation(final String category, final String value, final Object... args) {
        return Component.translatable(createTranslationKey(category, value), args);
    }

    public static MutableComponent createStoredWithCapacityTranslation(
        final long stored,
        final long capacity,
        final double pct
    ) {
        return createTranslation(
            "misc",
            "stored_with_capacity",
            Component.literal(stored == Long.MAX_VALUE ? "∞" : AmountFormatting.format(stored))
                .withStyle(ChatFormatting.WHITE),
            Component.literal(capacity == Long.MAX_VALUE ? "∞" : AmountFormatting.format(capacity))
                .withStyle(ChatFormatting.WHITE),
            Component.literal(String.valueOf((int) (pct * 100D)))
        ).withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent createTranslationAsHeading(final String category, final String value) {
        return Component.literal("<")
            .append(createTranslation(category, value))
            .append(">")
            .withStyle(ChatFormatting.DARK_GRAY);
    }
}
