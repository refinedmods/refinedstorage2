package com.refinedmods.refinedstorage2.core.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Quantities {
    private static final DecimalFormat FORMATTER = new DecimalFormat("####0.#", DecimalFormatSymbols.getInstance(Locale.US));

    private Quantities() {
    }

    public static String formatWithUnits(long qty) {
        if (qty >= 1_000_000_000) {
            return FORMATTER.format(qty / 1_000_000_000F) + "B";
        } else if (qty >= 1_000_000) {
            if (qty >= 100_000_000) {
                return FORMATTER.format(Math.floor(qty / 1_000_000F)) + "M";
            }
            return FORMATTER.format(qty / 1_000_000F) + "M";
        } else if (qty >= 1000) {
            if (qty >= 100_000) {
                return FORMATTER.format(Math.floor(qty / 1000F)) + "K";
            }
            return FORMATTER.format(qty / 1000F) + "K";
        }
        return String.valueOf(qty);
    }
}
