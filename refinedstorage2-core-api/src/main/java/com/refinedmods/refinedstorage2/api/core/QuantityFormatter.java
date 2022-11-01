package com.refinedmods.refinedstorage2.api.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public final class QuantityFormatter {
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

    private QuantityFormatter() {
    }

    public static String formatWithUnits(final long qty) {
        if (qty >= 1_000_000_000) {
            return formatBillion(qty);
        } else if (qty >= 1_000_000) {
            return formatMillion(qty);
        } else if (qty >= 1000) {
            return formatThousand(qty);
        }
        return String.valueOf(qty);
    }

    private static String formatBillion(final long qty) {
        return FORMATTER_WITH_UNITS.format(qty / 1_000_000_000D) + "B";
    }

    private static String formatMillion(final long qty) {
        if (qty >= 100_000_000) {
            return FORMATTER_WITH_UNITS.format(Math.floor(qty / 1_000_000D)) + "M";
        }
        return FORMATTER_WITH_UNITS.format(qty / 1_000_000D) + "M";
    }

    private static String formatThousand(final long qty) {
        if (qty >= 100_000) {
            return FORMATTER_WITH_UNITS.format(Math.floor(qty / 1000D)) + "K";
        }
        return FORMATTER_WITH_UNITS.format(qty / 1000D) + "K";
    }

    public static String format(final long qty) {
        return FORMATTER.format(qty);
    }
}
