package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.ItemStorageNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistryImpl;
import com.refinedmods.refinedstorage2.core.network.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.node.EmptyNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class NetworkUtil {
    private static class NodeCallbackListenerComponent implements NetworkComponent {
        private final List<NetworkNodeContainer<?>> added = new ArrayList<>();
        private final List<NetworkNodeContainer<?>> removed = new ArrayList<>();
        private final List<Set<Network>> splits = new ArrayList<>();
        private final List<Network> merges = new ArrayList<>();
        private int removeCount = 0;

        @Override
        public void onContainerAdded(NetworkNodeContainer<?> container) {
            added.add(container);
        }

        @Override
        public void onContainerRemoved(NetworkNodeContainer<?> container) {
            removed.add(container);
        }

        @Override
        public void onNetworkRemoved() {
            removeCount++;
        }

        @Override
        public void onNetworkSplit(Set<Network> networks) {
            splits.add(networks);
        }

        @Override
        public void onNetworkMerge(Network network) {
            merges.add(network);
        }
    }

    public static final NetworkComponentRegistry NETWORK_COMPONENT_REGISTRY = new NetworkComponentRegistryImpl();

    static {
        NETWORK_COMPONENT_REGISTRY.addComponent(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        NETWORK_COMPONENT_REGISTRY.addComponent(GraphNetworkComponent.class, GraphNetworkComponent::new);
        NETWORK_COMPONENT_REGISTRY.addComponent(ItemStorageNetworkComponent.class, network -> new ItemStorageNetworkComponent());
        NETWORK_COMPONENT_REGISTRY.addComponent(NodeCallbackListenerComponent.class, network -> new NodeCallbackListenerComponent());
    }

    public static Network createWithCreativeEnergySource() {
        Network network = new NetworkImpl(NETWORK_COMPONENT_REGISTRY);
        network.getComponent(EnergyNetworkComponent.class).getEnergyStorage().addSource(new CreativeEnergyStorage());
        return network;
    }

    public static NetworkNodeContainer<?> createContainer(Rs2World world, Position position) {
        return new NetworkNodeContainerImpl<>(world, position, new EmptyNetworkNode(world, position));
    }

    public static NetworkNodeContainer<?> createContainerWithNetwork(Rs2World world, Position position, Function<NetworkNodeContainer<?>, Network> networkFactory) {
        NetworkNodeContainer<?> container = createContainer(world, position);
        Network network = networkFactory.apply(container);
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return container;
    }

    public static NetworkNodeContainer<?> createContainerWithNetwork(Rs2World world, Position position) {
        return createContainerWithNetwork(world, position, container -> new NetworkImpl(NetworkUtil.NETWORK_COMPONENT_REGISTRY));
    }

    public static List<NetworkNodeContainer<?>> getAddedContainers(Network network) {
        return network.getComponent(NodeCallbackListenerComponent.class).added;
    }

    public static List<NetworkNodeContainer<?>> getRemovedContainers(Network network) {
        return network.getComponent(NodeCallbackListenerComponent.class).removed;
    }

    public static List<Set<Network>> getNetworkSplits(Network network) {
        return network.getComponent(NodeCallbackListenerComponent.class).splits;
    }

    public static List<Network> getNetworkMerges(Network network) {
        return network.getComponent(NodeCallbackListenerComponent.class).merges;
    }

    public static int getNetworkRemovedCount(Network network) {
        return network.getComponent(NodeCallbackListenerComponent.class).removeCount;
    }
}
