package com.refinedmods.refinedstorage2.api.core;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class QuantityFormatterTest {
    @Test
    void Test_formatting_with_units_for_small_number() {
        assertThat(QuantityFormatter.formatWithUnits(0)).isEqualTo("0");
        assertThat(QuantityFormatter.formatWithUnits(1)).isEqualTo("1");
        assertThat(QuantityFormatter.formatWithUnits(10)).isEqualTo("10");
        assertThat(QuantityFormatter.formatWithUnits(105)).isEqualTo("105");
    }

    @Test
    void Test_formatting_with_units_for_number_in_the_thousands() {
        assertThat(QuantityFormatter.formatWithUnits(1000)).isEqualTo("1K");
        assertThat(QuantityFormatter.formatWithUnits(1510)).isEqualTo("1.5K");

        assertThat(QuantityFormatter.formatWithUnits(10_000)).isEqualTo("10K");
        assertThat(QuantityFormatter.formatWithUnits(10_510)).isEqualTo("10.5K");

        assertThat(QuantityFormatter.formatWithUnits(100_000)).isEqualTo("100K");
        assertThat(QuantityFormatter.formatWithUnits(100_500)).isEqualTo("100K");
        assertThat(QuantityFormatter.formatWithUnits(100_999)).isEqualTo("100K");

        assertThat(QuantityFormatter.formatWithUnits(101_000)).isEqualTo("101K");
        assertThat(QuantityFormatter.formatWithUnits(101_500)).isEqualTo("101K");
        assertThat(QuantityFormatter.formatWithUnits(101_999)).isEqualTo("101K");
    }

    @Test
    void Test_formatting_with_units_for_number_in_the_millions() {
        assertThat(QuantityFormatter.formatWithUnits(1_000_000)).isEqualTo("1M");
        assertThat(QuantityFormatter.formatWithUnits(1_510_000)).isEqualTo("1.5M");

        assertThat(QuantityFormatter.formatWithUnits(10_000_000)).isEqualTo("10M");
        assertThat(QuantityFormatter.formatWithUnits(10_510_000)).isEqualTo("10.5M");

        assertThat(QuantityFormatter.formatWithUnits(100_000_000)).isEqualTo("100M");
        assertThat(QuantityFormatter.formatWithUnits(100_510_000)).isEqualTo("100M");
        assertThat(QuantityFormatter.formatWithUnits(100_999_000)).isEqualTo("100M");

        assertThat(QuantityFormatter.formatWithUnits(101_000_000)).isEqualTo("101M");
        assertThat(QuantityFormatter.formatWithUnits(101_510_000)).isEqualTo("101M");
        assertThat(QuantityFormatter.formatWithUnits(101_999_000)).isEqualTo("101M");
    }

    @Test
    void Test_formatting_with_units_for_number_in_the_billions() {
        assertThat(QuantityFormatter.formatWithUnits(1_000_000_000)).isEqualTo("1B");
        assertThat(QuantityFormatter.formatWithUnits(1_010_000_000)).isEqualTo("1B");
        assertThat(QuantityFormatter.formatWithUnits(1_100_001_000)).isEqualTo("1.1B");
        assertThat(QuantityFormatter.formatWithUnits(1_920_001_000)).isEqualTo("1.9B");
    }

    @Test
    void Test_formatting() {
        assertThat(QuantityFormatter.format(0)).isEqualTo("0");
        assertThat(QuantityFormatter.format(1)).isEqualTo("1");
        assertThat(QuantityFormatter.format(10)).isEqualTo("10");
        assertThat(QuantityFormatter.format(105)).isEqualTo("105");
        assertThat(QuantityFormatter.format(1050)).isEqualTo("1,050");
        assertThat(QuantityFormatter.format(10500)).isEqualTo("10,500");
        assertThat(QuantityFormatter.format(100500)).isEqualTo("100,500");
        assertThat(QuantityFormatter.format(1000500)).isEqualTo("1,000,500");
    }
}
