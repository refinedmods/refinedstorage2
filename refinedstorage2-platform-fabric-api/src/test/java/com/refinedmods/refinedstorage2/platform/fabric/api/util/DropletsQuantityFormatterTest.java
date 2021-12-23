package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@Rs2Test
class DropletsQuantityFormatterTest {
    @Test
    void Test_formatting_as_bucket_with_units_for_complete_buckets() {
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET)).isEqualTo("1");
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET * 2)).isEqualTo("2");
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET * 3)).isEqualTo("3");
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET * 1000)).isEqualTo("1K");
    }

    @Test
    void Test_formatting_as_bucket_with_units_for_partial_buckets() {
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET + (FluidConstants.BUCKET / 2))).isEqualTo("1");
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET + 1)).isEqualTo("1");
    }

    @Test
    void Test_formatting_as_bucket_with_units_for_less_than_1_bucket() {
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET / 2)).isEqualTo("0.5");
        assertThat(DropletsQuantityFormatter.formatAsBucketWithUnits(FluidConstants.BUCKET / 3)).isEqualTo("0.3");
    }

    @Test
    void Test_formatting_as_bucket() {
        assertThat(DropletsQuantityFormatter.formatAsBucket(FluidConstants.BUCKET)).isEqualTo("1");
        assertThat(DropletsQuantityFormatter.formatAsBucket(FluidConstants.BUCKET + (FluidConstants.BUCKET / 2))).isEqualTo("1.5");
        assertThat(DropletsQuantityFormatter.formatAsBucket(FluidConstants.BUCKET + (FluidConstants.BUCKET / 3))).isEqualTo("1.3");
        assertThat(DropletsQuantityFormatter.formatAsBucket(FluidConstants.BUCKET * 1000)).isEqualTo("1,000");
        assertThat(DropletsQuantityFormatter.formatAsBucket((FluidConstants.BUCKET * 1000) + (FluidConstants.BUCKET / 3))).isEqualTo("1,000.3");
    }
}
