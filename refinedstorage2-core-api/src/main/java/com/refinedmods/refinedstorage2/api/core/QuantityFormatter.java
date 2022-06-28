package com.refinedmods.refinedstorage2.api.core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class QuantityFormatter {
    private static final DecimalFormat FORMATTER_WITH_UNITS = new DecimalFormat(
            "####0.#",
            DecimalFormatSymbols.getInstance(Locale.US)
    );
    private static final DecimalFormat FORMATTER = new DecimalFormat(
            "#,###",
            DecimalFormatSymbols.getInstance(Locale.US)
    );

    private QuantityFormatter() {
    }

    public static String formatWithUnits(final long qty) {
        if (qty >= 1_000_000_000) {
            return FORMATTER_WITH_UNITS.format(qty / 1_000_000_000F) + "B";
        } else if (qty >= 1_000_000) {
            if (qty >= 100_000_000) {
                return FORMATTER_WITH_UNITS.format(Math.floor(qty / 1_000_000F)) + "M";
            }
            return FORMATTER_WITH_UNITS.format(qty / 1_000_000F) + "M";
        } else if (qty >= 1000) {
            if (qty >= 100_000) {
                return FORMATTER_WITH_UNITS.format(Math.floor(qty / 1000F)) + "K";
            }
            return FORMATTER_WITH_UNITS.format(qty / 1000F) + "K";
        }
        return String.valueOf(qty);
    }

    public static String format(final long qty) {
        return FORMATTER.format(qty);
    }
}
