package com.refinedmods.refinedstorage.common.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public final class IdentifierUtil {
    public static final String MOD_ID = "refinedstorage";

    public static final MutableComponent YES = Component.translatable("gui.yes");
    public static final MutableComponent NO = Component.translatable("gui.no");

    private static final DecimalFormat FORMATTER_WITH_UNITS = new DecimalFormat(
        "####0.#",
        DecimalFormatSymbols.getInstance(Locale.US)
    );
    private static final DecimalFormat FORMATTER = new DecimalFormat(
        "#,###",
        DecimalFormatSymbols.getInstance(Locale.US)
    );

    static {
        FORMATTER_WITH_UNITS.setRoundingMode(RoundingMode.FLOOR);
    }

    private IdentifierUtil() {
    }

    public static ResourceLocation createIdentifier(final String value) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, value);
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
            Component.literal(stored == Long.MAX_VALUE ? "∞" : format(stored)).withStyle(ChatFormatting.WHITE),
            Component.literal(capacity == Long.MAX_VALUE ? "∞" : format(capacity)).withStyle(ChatFormatting.WHITE),
            Component.literal(String.valueOf((int) (pct * 100D)))
        ).withStyle(ChatFormatting.GRAY);
    }

    public static MutableComponent createTranslationAsHeading(final String category, final String value) {
        return Component.literal("<")
            .append(createTranslation(category, value))
            .append(">")
            .withStyle(ChatFormatting.DARK_GRAY);
    }

    // https://github.com/emilyploszaj/emi/blob/ee35c78b0f5b0b1e91cc0ba1571df8dcd88cbef3/xplat/src/main/java/dev/emi/emi/registry/EmiTags.java#L174
    public static String getTagTranslationKey(final TagKey<?> key) {
        final ResourceLocation registry = key.registry().location();
        final String fixedPath = registry.getPath().replace('/', '.');
        if (registry.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            return getTagTranslationKey("tag.%s.".formatted(fixedPath), key.location());
        }
        return getTagTranslationKey(
            "tag.%s.%s.".formatted(registry.getNamespace(), fixedPath),
            key.location()
        );
    }

    private static String getTagTranslationKey(final String prefix, final ResourceLocation id) {
        final String fixedPath = id.getPath().replace('/', '.');
        return prefix + id.getNamespace() + "." + fixedPath;
    }

    public static String formatWithUnits(final double qty) {
        if (qty >= 1_000_000_000) {
            return formatBillion((long) qty);
        } else if (qty >= 1_000_000) {
            return formatMillion((long) qty);
        } else if (qty >= 1000) {
            return formatThousand((long) qty);
        } else if (qty < 1) {
            return formatThousandth(qty);
        }
        return String.valueOf((long) qty);
    }

    private static String formatThousandth(final double qty) {
        return FORMATTER_WITH_UNITS.format(qty * 1_000) + "m";
    }

    private static String formatBillion(final double qty) {
        return FORMATTER_WITH_UNITS.format(qty / 1_000_000_000D) + "B";
    }

    private static String formatMillion(final double qty) {
        if (qty >= 100_000_000) {
            return FORMATTER_WITH_UNITS.format(Math.floor(qty / 1_000_000D)) + "M";
        }
        return FORMATTER_WITH_UNITS.format(qty / 1_000_000D) + "M";
    }

    private static String formatThousand(final double qty) {
        if (qty >= 100_000) {
            return FORMATTER_WITH_UNITS.format(Math.floor(qty / 1000D)) + "K";
        }
        return FORMATTER_WITH_UNITS.format(qty / 1000D) + "K";
    }

    public static String format(final long qty) {
        return FORMATTER.format(qty);
    }
}
