package com.refinedmods.refinedstorage2.api.stack.item;

import com.refinedmods.refinedstorage2.test.Rs2Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@Rs2Test
class Rs2ItemStackIdentifierTest {
    @Test
    void Test_equals_hashcode() {
        // Assert
        EqualsVerifier.forClass(Rs2ItemStackIdentifier.class).verify();
    }
}
