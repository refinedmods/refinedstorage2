package com.refinedmods.refinedstorage2.core.network.node.container;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@Rs2Test
class NetworkNodeContainerEntryTest {
    @Test
    void Test_equals_hashcode() {
        // Assert
        EqualsVerifier.forClass(NetworkNodeContainerEntry.class).withIgnoredFields("container").verify();
    }
}
