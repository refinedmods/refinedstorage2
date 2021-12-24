package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkImpl;
import com.refinedmods.refinedstorage2.test.Rs2Test;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class NetworkComponentRegistryImplTest {
    private record FakeComponent(Network network) implements NetworkComponent {
    }

    @Test
    void Test_should_register_component() {
        // Arrange
        NetworkComponentRegistry sut = new NetworkComponentRegistryImpl();

        Network network = new NetworkImpl(sut);

        // Act
        sut.addComponent(FakeComponent.class, FakeComponent::new);

        Map<Class<? extends NetworkComponent>, NetworkComponent> map = sut.buildComponentMap(network);

        // Assert
        assertThat(map).hasSize(1);
        assertThat(map.get(FakeComponent.class)).isNotNull().isInstanceOf(FakeComponent.class);
        assertThat(((FakeComponent) map.get(FakeComponent.class)).network).isSameAs(network);
    }
}
