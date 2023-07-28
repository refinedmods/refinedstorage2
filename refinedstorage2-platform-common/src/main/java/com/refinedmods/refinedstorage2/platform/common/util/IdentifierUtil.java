package com.refinedmods.refinedstorage2.platform.common.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public final class IdentifierUtil {
    public static final String MOD_ID = "refinedstorage2";

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

    public static MutableComponent createTranslationAsHeading(final String category, final String value) {
        return Component.literal("<")
            .append(createTranslation(category, value))
            .append(">")
            .withStyle(ChatFormatting.DARK_GRAY);
    }
}
