package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class QuantitiesTest {
    @Test
    void Test_formatting_with_units_for_small_number() {
        assertThat(Quantities.formatWithUnits(0)).isEqualTo("0");
        assertThat(Quantities.formatWithUnits(1)).isEqualTo("1");
        assertThat(Quantities.formatWithUnits(10)).isEqualTo("10");
        assertThat(Quantities.formatWithUnits(105)).isEqualTo("105");
    }

    @Test
    void Test_formatting_with_units_for_number_in_the_thousands() {
        assertThat(Quantities.formatWithUnits(1000)).isEqualTo("1K");
        assertThat(Quantities.formatWithUnits(1510)).isEqualTo("1.5K");

        assertThat(Quantities.formatWithUnits(10_000)).isEqualTo("10K");
        assertThat(Quantities.formatWithUnits(10_510)).isEqualTo("10.5K");

        assertThat(Quantities.formatWithUnits(100_000)).isEqualTo("100K");
        assertThat(Quantities.formatWithUnits(100_500)).isEqualTo("100K");
        assertThat(Quantities.formatWithUnits(100_999)).isEqualTo("100K");

        assertThat(Quantities.formatWithUnits(101_000)).isEqualTo("101K");
        assertThat(Quantities.formatWithUnits(101_500)).isEqualTo("101K");
        assertThat(Quantities.formatWithUnits(101_999)).isEqualTo("101K");
    }

    @Test
    void Test_formatting_with_units_for_number_in_the_millions() {
        assertThat(Quantities.formatWithUnits(1_000_000)).isEqualTo("1M");
        assertThat(Quantities.formatWithUnits(1_510_000)).isEqualTo("1.5M");

        assertThat(Quantities.formatWithUnits(10_000_000)).isEqualTo("10M");
        assertThat(Quantities.formatWithUnits(10_510_000)).isEqualTo("10.5M");

        assertThat(Quantities.formatWithUnits(100_000_000)).isEqualTo("100M");
        assertThat(Quantities.formatWithUnits(100_510_000)).isEqualTo("100M");
        assertThat(Quantities.formatWithUnits(100_999_000)).isEqualTo("100M");

        assertThat(Quantities.formatWithUnits(101_000_000)).isEqualTo("101M");
        assertThat(Quantities.formatWithUnits(101_510_000)).isEqualTo("101M");
        assertThat(Quantities.formatWithUnits(101_999_000)).isEqualTo("101M");
    }

    @Test
    void Test_formatting_with_units_for_number_in_the_billions() {
        assertThat(Quantities.formatWithUnits(1_000_000_000)).isEqualTo("1B");
        assertThat(Quantities.formatWithUnits(1_010_000_000)).isEqualTo("1B");
        assertThat(Quantities.formatWithUnits(1_100_001_000)).isEqualTo("1.1B");
        assertThat(Quantities.formatWithUnits(1_920_001_000)).isEqualTo("1.9B");
    }

    @Test
    void Test_formatting() {
        assertThat(Quantities.format(0)).isEqualTo("0");
        assertThat(Quantities.format(1)).isEqualTo("1");
        assertThat(Quantities.format(10)).isEqualTo("10");
        assertThat(Quantities.format(105)).isEqualTo("105");
        assertThat(Quantities.format(1050)).isEqualTo("1,050");
        assertThat(Quantities.format(10500)).isEqualTo("10,500");
        assertThat(Quantities.format(100500)).isEqualTo("100,500");
        assertThat(Quantities.format(1000500)).isEqualTo("1,000,500");
    }
}
