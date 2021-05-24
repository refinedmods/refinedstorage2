package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@Rs2Test
class NetworkNodeHostEntryTest {
    @Test
    void Test_equals_hashcode() {
        // Assert
        EqualsVerifier.forClass(NetworkNodeHostEntry.class).withIgnoredFields("host").verify();
    }
}
