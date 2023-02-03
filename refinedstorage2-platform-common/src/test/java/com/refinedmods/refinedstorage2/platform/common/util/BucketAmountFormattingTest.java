package com.refinedmods.refinedstorage2.platform.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BucketAmountFormattingTest {
    private static final long BUCKET_AMOUNT = 1000;

    private final BucketAmountFormatting sut = new BucketAmountFormatting(BUCKET_AMOUNT);

    @Test
    void shouldFormatWithUnitsForCompleteBuckets() {
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT)).isEqualTo("1");
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT * 2)).isEqualTo("2");
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT * 3)).isEqualTo("3");
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT * 1000)).isEqualTo("1K");
    }

    @Test
    void shouldFormatWithUnitsForPartialBuckets() {
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT + (BUCKET_AMOUNT / 2))).isEqualTo("1");
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT + 1)).isEqualTo("1");
    }

    @Test
    void shouldFormatWithUnitsForLessThan1Bucket() {
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT / 2)).isEqualTo("0.5");
        assertThat(sut.formatWithUnits(BUCKET_AMOUNT / 3)).isEqualTo("0.3");
    }

    @Test
    void shouldFormatWithoutUnits() {
        assertThat(sut.format(BUCKET_AMOUNT)).isEqualTo("1");
        assertThat(sut.format(BUCKET_AMOUNT + (BUCKET_AMOUNT / 2))).isEqualTo("1.5");
        assertThat(sut.format(BUCKET_AMOUNT + (BUCKET_AMOUNT / 3))).isEqualTo("1.3");
        assertThat(sut.format(BUCKET_AMOUNT * 1000)).isEqualTo("1,000");
        assertThat(sut.format((BUCKET_AMOUNT * 1000) + (BUCKET_AMOUNT / 3))).isEqualTo("1,000.3");
    }
}
