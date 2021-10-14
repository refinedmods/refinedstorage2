package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistryImpl;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.InfiniteEnergyStorage;
import com.refinedmods.refinedstorage2.api.network.node.EmptyNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistryImpl;

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
    public static final StorageChannelTypeRegistry STORAGE_CHANNEL_TYPE_REGISTRY = new StorageChannelTypeRegistryImpl();

    static {
        STORAGE_CHANNEL_TYPE_REGISTRY.addType(StorageChannelTypes.FAKE);

        NETWORK_COMPONENT_REGISTRY.addComponent(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        NETWORK_COMPONENT_REGISTRY.addComponent(GraphNetworkComponent.class, GraphNetworkComponent::new);
        NETWORK_COMPONENT_REGISTRY.addComponent(StorageNetworkComponent.class, network -> new StorageNetworkComponent(STORAGE_CHANNEL_TYPE_REGISTRY));
        NETWORK_COMPONENT_REGISTRY.addComponent(NodeCallbackListenerComponent.class, network -> new NodeCallbackListenerComponent());
    }

    public static Network createWithInfiniteEnergyStorage() {
        Network network = new NetworkImpl(NETWORK_COMPONENT_REGISTRY);
        network.getComponent(EnergyNetworkComponent.class).getEnergyStorage().addSource(new InfiniteEnergyStorage());
        return network;
    }

    public static NetworkNodeContainer<?> createContainer() {
        return new NetworkNodeContainerImpl<>(new EmptyNetworkNode());
    }

    public static NetworkNodeContainer<?> createContainerWithNetwork(Function<NetworkNodeContainer<?>, Network> networkFactory) {
        NetworkNodeContainer<?> container = createContainer();
        Network network = networkFactory.apply(container);
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return container;
    }

    public static NetworkNodeContainer<?> createContainerWithNetwork() {
        return createContainerWithNetwork(container -> new NetworkImpl(NETWORK_COMPONENT_REGISTRY));
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

    public static StorageNetworkComponent storageComponentOf(Network network) {
        return network.getComponent(StorageNetworkComponent.class);
    }

    public static StorageChannel<String> fakeStorageChannelOf(Network network) {
        return storageComponentOf(network).getStorageChannel(StorageChannelTypes.FAKE);
    }
}
