package com.refinedmods.refinedstorage2.core.network.node;

import com.refinedmods.refinedstorage2.core.RefinedStorage2Test;
import net.minecraft.util.math.BlockPos;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@RefinedStorage2Test
class StubNetworkNodeReferenceTest {
    @Test
    void Test_equal() {
        // Arrange
        FakeNetworkNode node = new FakeNetworkNode(BlockPos.ORIGIN);

        StubNetworkNodeReference a = new StubNetworkNodeReference(node);
        StubNetworkNodeReference b = new StubNetworkNodeReference(node);

        // Assert
        assertThat(a).isEqualTo(b);
    }

    @Test
    void Test_not_equal() {
        // Arrange
        StubNetworkNodeReference a = new StubNetworkNodeReference(new FakeNetworkNode(BlockPos.ORIGIN));
        StubNetworkNodeReference b = new StubNetworkNodeReference(new FakeNetworkNode(BlockPos.ORIGIN));

        // Assert
        assertThat(a).isNotEqualTo(b);
    }
}
