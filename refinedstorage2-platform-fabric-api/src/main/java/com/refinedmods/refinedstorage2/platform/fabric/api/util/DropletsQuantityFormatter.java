package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public final class DropletsQuantityFormatter {
    private static final DecimalFormat LESS_THAN_1_BUCKET_FORMATTER = new DecimalFormat("####0.#", DecimalFormatSymbols.getInstance(Locale.US));

    private DropletsQuantityFormatter() {
    }

    public static String formatDropletsAsBucket(long droplets) {
        double buckets = droplets / (double) FluidConstants.BUCKET;
        if (buckets >= 1) {
            return QuantityFormatter.formatWithUnits((long) Math.floor(buckets));
        } else {
            return LESS_THAN_1_BUCKET_FORMATTER.format(buckets);
        }
    }
}
