package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.platform.common.PlatformProxy;
import com.refinedmods.refinedstorage2.platform.common.TestPlatform;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FluidResourceRenderingTest {
    private static final long BUCKET_AMOUNT = 1000;

    @BeforeAll
    static void setUp() {
        PlatformProxy.loadPlatform(new TestPlatform(BUCKET_AMOUNT));
    }

    @Test
    void shouldFormatWithUnitsForCompleteBuckets() {
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT)).isEqualTo("1");
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT * 2)).isEqualTo("2");
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT * 3)).isEqualTo("3");
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT * 1000)).isEqualTo("1K");
    }

    @Test
    void shouldFormatWithUnitsForPartialBuckets() {
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT + (BUCKET_AMOUNT / 2))).isEqualTo("1");
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT + 1)).isEqualTo("1");
    }

    @Test
    void shouldFormatWithUnitsForLessThan1Bucket() {
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT / 2)).isEqualTo("0.5");
        assertThat(FluidResourceRendering.formatWithUnits(BUCKET_AMOUNT / 3)).isEqualTo("0.3");
    }

    @Test
    void shouldFormatWithoutUnits() {
        assertThat(FluidResourceRendering.format(BUCKET_AMOUNT)).isEqualTo("1");
        assertThat(FluidResourceRendering.format(BUCKET_AMOUNT + (BUCKET_AMOUNT / 2))).isEqualTo("1.5");
        assertThat(FluidResourceRendering.format(BUCKET_AMOUNT + (BUCKET_AMOUNT / 3))).isEqualTo("1.3");
        assertThat(FluidResourceRendering.format(BUCKET_AMOUNT * 1000)).isEqualTo("1,000");
        assertThat(FluidResourceRendering.format((BUCKET_AMOUNT * 1000) + (BUCKET_AMOUNT / 3))).isEqualTo("1,000.3");
    }
}
