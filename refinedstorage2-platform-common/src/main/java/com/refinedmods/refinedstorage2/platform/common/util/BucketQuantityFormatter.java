package com.refinedmods.refinedstorage2.platform.common.util;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public final class BucketQuantityFormatter {
    private static final DecimalFormat LESS_THAN_1_BUCKET_FORMATTER = new DecimalFormat("0.#", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.#", DecimalFormatSymbols.getInstance(Locale.US));

    private final long bucketAmount;

    public BucketQuantityFormatter(long bucketAmount) {
        this.bucketAmount = bucketAmount;
    }

    public String formatWithUnits(long droplets) {
        double buckets = convertToBuckets(droplets);
        if (buckets >= 1) {
            return QuantityFormatter.formatWithUnits((long) Math.floor(buckets));
        } else {
            return LESS_THAN_1_BUCKET_FORMATTER.format(buckets);
        }
    }

    public String format(long droplets) {
        double buckets = convertToBuckets(droplets);
        return FORMATTER.format(buckets);
    }

    private double convertToBuckets(long droplets) {
        return droplets / (double) bucketAmount;
    }
}
