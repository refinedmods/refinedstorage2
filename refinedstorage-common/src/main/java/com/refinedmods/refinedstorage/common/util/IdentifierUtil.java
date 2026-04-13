package com.refinedmods.refinedstorage.common.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public final class IdentifierUtil {
    public static final String MOD_ID = "refinedstorage";
    public static final MutableComponent MOD_NAME = Component.translatable("mod." + MOD_ID);

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

    public static Identifier createIdentifier(final String value) {
        return Identifier.fromNamespaceAndPath(MOD_ID, value);
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
        final Identifier registry = key.registry().identifier();
        final String fixedPath = registry.getPath().replace('/', '.');
        if (registry.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            return getTagTranslationKey("tag.%s.".formatted(fixedPath), key.location());
        }
        return getTagTranslationKey(
            "tag.%s.%s.".formatted(registry.getNamespace(), fixedPath),
            key.location()
        );
    }

    private static String getTagTranslationKey(final String prefix, final Identifier id) {
        final String fixedPath = id.getPath().replace('/', '.');
        return prefix + id.getNamespace() + "." + fixedPath;
    }

    public static String formatWithUnits(final double qty) {
        if (qty < 0.001) {
            return "0";
        }

        return switch ((int) Math.floor(Math.log10(qty) / 3)) {
            case -1 -> FORMATTER_WITH_UNITS.format(qty * 10e2) + "m";
            case 0 -> FORMATTER_WITH_UNITS.format(qty >= 100 ? Math.floor(qty) : qty);
            case 1 -> FORMATTER_WITH_UNITS.format(qty >= 10e4 ? Math.floor(qty / 10e2) : qty / 10e2) + "k";
            case 2 -> FORMATTER_WITH_UNITS.format(qty >= 10e7 ? Math.floor(qty / 10e5) : qty / 10e5) + "M";
            case 3 -> FORMATTER_WITH_UNITS.format(qty >= 10e10 ? Math.floor(qty / 10e8) : qty / 10e8) + "G";
            case 4 -> FORMATTER_WITH_UNITS.format(qty >= 10e13 ? Math.floor(qty / 10e11) : qty / 10e11) + "T";
            case 5 -> FORMATTER_WITH_UNITS.format(qty >= 10e16 ? Math.floor(qty / 10e14) : qty / 10e14) + "P";
            case 6 -> FORMATTER_WITH_UNITS.format(qty >= 10e19 ? Math.floor(qty / 10e17) : qty / 10e17) + "E";
            default -> "∞";
        };
    }

    public static String format(final long qty) {
        return FORMATTER.format(qty);
    }
}
