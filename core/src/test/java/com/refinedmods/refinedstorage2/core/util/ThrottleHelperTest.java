package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.core.Rs2Test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class ThrottleHelperTest {
    @Test
    void Test_should_throttle_correctly_with_single_key() {
        ThrottleHelper<String> throttleHelper = new ThrottleHelper<>(500);

        assertThat(throttleHelper.throttle("key 1", () -> {
        }, 0)).isTrue();
        assertThat(throttleHelper.throttle("key 1", Assertions::fail, 100)).isFalse();
        assertThat(throttleHelper.throttle("key 1", Assertions::fail, 499)).isFalse();
        assertThat(throttleHelper.throttle("key 1", () -> {
        }, 500)).isTrue();
    }

    @Test
    void Test_should_throttle_correctly_with_multiple_keys() {
        ThrottleHelper<String> throttleHelper = new ThrottleHelper<>(500);

        assertThat(throttleHelper.throttle("key 1", () -> {
        }, 0)).isTrue();
        assertThat(throttleHelper.throttle("key 2", () -> {
        }, 100)).isTrue();

        assertThat(throttleHelper.throttle("key 1", Assertions::fail, 100)).isFalse();
        assertThat(throttleHelper.throttle("key 1", Assertions::fail, 499)).isFalse();
        assertThat(throttleHelper.throttle("key 2", Assertions::fail, 200)).isFalse();
        assertThat(throttleHelper.throttle("key 2", Assertions::fail, 599)).isFalse();

        assertThat(throttleHelper.throttle("key 1", () -> {
        }, 500)).isTrue();
        assertThat(throttleHelper.throttle("key 2", () -> {
        }, 600)).isTrue();
    }
}
