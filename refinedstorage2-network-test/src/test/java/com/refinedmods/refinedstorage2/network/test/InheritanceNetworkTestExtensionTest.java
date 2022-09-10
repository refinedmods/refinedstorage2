package com.refinedmods.refinedstorage2.network.test;

import com.refinedmods.refinedstorage2.api.network.Network;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InheritanceNetworkTestExtensionTest extends NetworkTestExtensionTest {
    @InjectNetwork("a")
    Network a2;

    @Test
    void shouldInitializeProperlyWithInheritance() {
        // Assert
        assertThat(a).isNotNull().isSameAs(a2);
        assertThat(b).isNotNull();
    }
}
