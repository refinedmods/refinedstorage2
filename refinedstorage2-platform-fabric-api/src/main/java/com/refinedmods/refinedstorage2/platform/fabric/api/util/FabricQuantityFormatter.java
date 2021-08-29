package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public final class FabricQuantityFormatter {
    private static final DecimalFormat BUCKET_FORMATTER = new DecimalFormat("#.#", DecimalFormatSymbols.getInstance(Locale.US));

    private FabricQuantityFormatter() {
    }

    public static String formatDropletsAsBucket(long droplets) {
        double buckets = droplets / (double) FluidConstants.BUCKET;
        return BUCKET_FORMATTER.format(buckets) + " B";
    }
}
