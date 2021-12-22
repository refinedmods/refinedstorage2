package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@Rs2Test
class DropletsQuantityFormatterTest {
    @Test
    void Test_formatting_complete_buckets() {
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET)).isEqualTo("1");
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET * 2)).isEqualTo("2");
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET * 3)).isEqualTo("3");
    }

    @Test
    void Test_formatting_partial_buckets() {
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET + (FluidConstants.BUCKET / 2))).isEqualTo("1");
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET + 1)).isEqualTo("1");
    }

    @Test
    void Test_formatting_less_than_1_bucket() {
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET / 2)).isEqualTo("0.5");
        assertThat(DropletsQuantityFormatter.formatDropletsAsBucket(FluidConstants.BUCKET / 3)).isEqualTo("0.3");
    }
}
