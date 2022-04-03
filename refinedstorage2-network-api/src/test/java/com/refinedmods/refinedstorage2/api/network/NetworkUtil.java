package com.refinedmods.refinedstorage2.api.network;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.api.network.node.EmptyNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.test.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public final class NetworkUtil {
    public static final ComponentMapFactory<NetworkComponent, Network> NETWORK_COMPONENT_MAP_FACTORY = new ComponentMapFactory<>();
    public static final StorageChannelTypeRegistry STORAGE_CHANNEL_TYPE_REGISTRY = new StorageChannelTypeRegistryImpl();

    static {
        STORAGE_CHANNEL_TYPE_REGISTRY.addType(StorageChannelTypes.FAKE);

        NETWORK_COMPONENT_MAP_FACTORY.addFactory(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(GraphNetworkComponent.class, GraphNetworkComponent::new);
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(StorageNetworkComponent.class, network -> new StorageNetworkComponent(STORAGE_CHANNEL_TYPE_REGISTRY));
        NETWORK_COMPONENT_MAP_FACTORY.addFactory(NodeCallbackListenerComponent.class, network -> new NodeCallbackListenerComponent());
    }

    private NetworkUtil() {
    }

    public static Network create(long energyStored, long energyCapacity) {
        Network network = new NetworkImpl(NETWORK_COMPONENT_MAP_FACTORY);
        EnergyNetworkComponent component = network.getComponent(EnergyNetworkComponent.class);
        EnergyStorage storage = new EnergyStorageImpl(energyCapacity);
        storage.receive(energyStored, Action.EXECUTE);
        ControllerNetworkNode controller = new ControllerNetworkNode();
        controller.setEnergyStorage(storage);
        component.onContainerAdded(() -> controller);
        return network;
    }

    public static Network create() {
        return create(Long.MAX_VALUE, Long.MAX_VALUE);
    }

    public static NetworkNodeContainer createContainer() {
        EmptyNetworkNode node = new EmptyNetworkNode();
        return () -> node;
    }

    public static NetworkNodeContainer createContainerWithNetwork(Function<NetworkNodeContainer, Network> networkFactory) {
        NetworkNodeContainer container = createContainer();
        Network network = networkFactory.apply(container);
        container.getNode().setNetwork(network);
        network.addContainer(container);
        return container;
    }

    public static NetworkNodeContainer createContainerWithNetwork() {
        return createContainerWithNetwork(container -> new NetworkImpl(NETWORK_COMPONENT_MAP_FACTORY));
    }

    public static List<NetworkNodeContainer> getAddedContainers(Network network) {
        return network.getComponent(NodeCallbackListenerComponent.class).added;
    }

    public static List<NetworkNodeContainer> getRemovedContainers(Network network) {
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

    private static class NodeCallbackListenerComponent implements NetworkComponent {
        private final List<NetworkNodeContainer> added = new ArrayList<>();
        private final List<NetworkNodeContainer> removed = new ArrayList<>();
        private final List<Set<Network>> splits = new ArrayList<>();
        private final List<Network> merges = new ArrayList<>();
        private int removeCount = 0;

        @Override
        public void onContainerAdded(NetworkNodeContainer container) {
            added.add(container);
        }

        @Override
        public void onContainerRemoved(NetworkNodeContainer container) {
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
        public void onNetworkMergedWith(Network network) {
            merges.add(network);
        }
    }
}
