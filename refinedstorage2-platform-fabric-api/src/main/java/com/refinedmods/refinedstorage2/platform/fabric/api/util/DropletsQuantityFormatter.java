package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public final class DropletsQuantityFormatter {
    private static final DecimalFormat LESS_THAN_1_BUCKET_FORMATTER = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.US));

    private DropletsQuantityFormatter() {
    }

    public static String formatAsBucketWithUnits(long droplets) {
        double buckets = convertToBuckets(droplets);
        if (buckets >= 1) {
            return QuantityFormatter.formatWithUnits((long) Math.floor(buckets));
        } else {
            return LESS_THAN_1_BUCKET_FORMATTER.format(buckets);
        }
    }

    public static String formatAsBucket(long droplets) {
        double buckets = convertToBuckets(droplets);
        return FORMATTER.format(buckets);
    }

    private static double convertToBuckets(long droplets) {
        return droplets / (double) FluidConstants.BUCKET;
    }
}
