package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.util.Position;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class StubNetworkNodeReferenceTest {
    @Test
    void Test_equal() {
        // Arrange
        FakeNetworkNode node = new FakeNetworkNode(Position.ORIGIN);

        StubNetworkNodeReference a = new StubNetworkNodeReference(node);
        StubNetworkNodeReference b = new StubNetworkNodeReference(node);

        // Assert
        assertThat(a).isEqualTo(b);
    }

    @Test
    void Test_not_equal() {
        // Arrange
        StubNetworkNodeReference a = new StubNetworkNodeReference(new FakeNetworkNode(Position.ORIGIN));
        StubNetworkNodeReference b = new StubNetworkNodeReference(new FakeNetworkNode(Position.ORIGIN));

        // Assert
        assertThat(a).isNotEqualTo(b);
    }
}
