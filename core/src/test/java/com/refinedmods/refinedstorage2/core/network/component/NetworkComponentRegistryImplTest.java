package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.Rs2Test;
import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkImpl;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Rs2Test
class NetworkComponentRegistryImplTest {
    private static class FakeComponent implements NetworkComponent {
        private final Network network;

        public FakeComponent(Network network) {
            this.network = network;
        }

        @Override
        public void onHostAdded(NetworkNodeHost<?> host) {

        }

        @Override
        public void onHostRemoved(NetworkNodeHost<?> host) {

        }

        @Override
        public void onNetworkRemoved() {

        }

        @Override
        public void onNetworkSplit(Set<Network> networks) {

        }
    }

    @Test
    void Test_should_register_component() {
        // Arrange
        NetworkComponentRegistry registry = new NetworkComponentRegistryImpl();

        Network network = new NetworkImpl(registry);

        // Act
        registry.addComponent(FakeComponent.class, FakeComponent::new);

        Map<Class<? extends NetworkComponent>, NetworkComponent> map = registry.buildComponentMap(network);

        // Assert
        assertThat(map).hasSize(1);
        assertThat(map.get(FakeComponent.class)).isNotNull().isInstanceOf(FakeComponent.class);
        assertThat(((FakeComponent) map.get(FakeComponent.class)).network).isSameAs(network);
    }
}
