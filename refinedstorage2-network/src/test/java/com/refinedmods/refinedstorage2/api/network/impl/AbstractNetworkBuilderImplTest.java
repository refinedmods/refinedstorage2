package com.refinedmods.refinedstorage2.api.network.impl;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.network.test.NetworkTestFixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;

import org.junit.jupiter.api.BeforeEach;

import static java.util.Objects.requireNonNull;

abstract class AbstractNetworkBuilderImplTest {
    protected NetworkBuilder sut;
    protected ComponentMapFactory<NetworkComponent, Network> componentMapFactory;

    @BeforeEach
    void setUp() {
        componentMapFactory = NetworkTestFixtures.NETWORK_COMPONENT_MAP_FACTORY.copy();
        componentMapFactory.addFactory(
            InterceptingNetworkComponent.class,
            network -> new InterceptingNetworkComponent()
        );
        sut = new NetworkBuilderImpl(new NetworkFactory(componentMapFactory));
    }

    protected void clearTracking(@Nullable final Network network) {
        requireNonNull(network).getComponent(InterceptingNetworkComponent.class).clear();
    }

    protected NetworkNodeContainer createContainerWithNetwork() {
        return createContainerWithNetwork(container -> new NetworkImpl(componentMapFactory));
    }

    protected static NetworkNodeContainer createContainerWithNetwork(
        final Function<NetworkNodeContainer, Network> networkFactory
    ) {
        return createContainerWithNetwork(createNode(), networkFactory, 0);
    }

    protected static NetworkNodeContainer createContainerWithNetwork(
        final NetworkNode networkNode,
        final Function<NetworkNodeContainer, Network> networkFactory,
        final int priority
    ) {
        final NetworkNodeContainer container = new NetworkNodeContainer() {
            @Override
            public NetworkNode getNode() {
                return networkNode;
            }

            @Override
            public int getPriority() {
                return priority;
            }
        };
        final Network network = networkFactory.apply(container);
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return container;
    }

    protected static NetworkNodeContainer createContainer() {
        final NetworkNode node = createNode();
        return () -> node;
    }

    private static NetworkNode createNode() {
        return new SpyingNetworkNode(0);
    }

    protected static List<NetworkNodeContainer> getAddedContainers(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).added;
    }

    protected static List<NetworkNodeContainer> getRemovedContainers(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).removed;
    }

    protected static List<Set<Network>> getNetworkSplits(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).splits;
    }

    protected static List<Network> getNetworkMerges(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).merges;
    }

    protected static int getAmountRemoved(final Network network) {
        return network.getComponent(InterceptingNetworkComponent.class).amountRemoved;
    }

    private static class InterceptingNetworkComponent implements NetworkComponent {
        private final List<NetworkNodeContainer> added = new ArrayList<>();
        private final List<NetworkNodeContainer> removed = new ArrayList<>();
        private final List<Set<Network>> splits = new ArrayList<>();
        private final List<Network> merges = new ArrayList<>();
        private int amountRemoved = 0;

        @Override
        public void onContainerAdded(final NetworkNodeContainer container) {
            added.add(container);
        }

        @Override
        public void onContainerRemoved(final NetworkNodeContainer container) {
            removed.add(container);
        }

        @Override
        public void onNetworkRemoved() {
            amountRemoved++;
        }

        @Override
        public void onNetworkSplit(final Set<Network> networks) {
            splits.add(networks);
        }

        @Override
        public void onNetworkMergedWith(final Network newMainNetwork) {
            merges.add(newMainNetwork);
        }

        public void clear() {
            added.clear();
            removed.clear();
            splits.clear();
            merges.clear();
            amountRemoved = 0;
        }
    }
}
