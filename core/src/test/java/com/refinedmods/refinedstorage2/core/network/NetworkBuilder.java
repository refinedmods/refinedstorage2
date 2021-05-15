package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.adapter.FakeRs2World;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.StubNetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.core.util.Position;

import java.util.UUID;

public class NetworkBuilder {
    private final Network network;

    public static NetworkBuilder create() {
        return new NetworkBuilder(new NetworkImpl(UUID.randomUUID()));
    }

    private NetworkBuilder(Network network) {
        this.network = network;
    }

    public NetworkBuilder node(NetworkNode node) {
        node.setNetwork(network);
        nodeRef(new StubNetworkNodeReference(node));
        return this;
    }

    public NetworkBuilder nodeRef(NetworkNodeReference ref) {
        ref.get().ifPresent(node -> node.setNetwork(network));
        network.getNodeReferences().add(ref);
        network.onNodesChanged();
        return this;
    }

    public NetworkBuilder infiniteEnergy() {
        energy(Long.MAX_VALUE, Long.MAX_VALUE);
        return this;
    }

    public NetworkBuilder energy(long stored, long capacity) {
        ControllerNetworkNode controller = new ControllerNetworkNode(new FakeRs2World(), Position.ORIGIN, null, stored, capacity, ControllerType.NORMAL);
        node(controller);
        return this;
    }

    public Network build() {
        return network;
    }
}
