package com.refinedmods.refinedstorage2.fabric.coreimpl.network;

import com.refinedmods.refinedstorage2.core.network.Network;
import com.refinedmods.refinedstorage2.core.network.NetworkImpl;
import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import com.refinedmods.refinedstorage2.core.network.NetworkManagerImpl;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeRepository;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

public class FabricNetworkManager extends PersistentState implements NetworkManager {
    public static final String NAME = "refinedstorage2_networks";

    private static final String TAG_NETWORKS = "networks";
    private static final String TAG_NETWORK_ID = "id";
    private static final String TAG_NETWORK_NODE_REFERENCES = "refs";

    private final NetworkManagerImpl parent;
    private final MinecraftServer server;

    public FabricNetworkManager(String name, NetworkManagerImpl parent, MinecraftServer server) {
        super(name);
        this.parent = parent;
        this.server = server;
    }

    @Override
    public Network onNodeAdded(NetworkNodeRepository nodeRepository, Position pos) {
        Network network = parent.onNodeAdded(nodeRepository, pos);
        markDirty();
        return network;
    }

    @Override
    public void onNodeRemoved(NetworkNodeRepository nodeRepository, Position pos) {
        parent.onNodeRemoved(nodeRepository, pos);
        markDirty();
    }

    @Override
    public Collection<Network> getNetworks() {
        return parent.getNetworks();
    }

    @Override
    public void update() {
        parent.update();
    }

    @Override
    public void fromTag(CompoundTag tag) {
        ListTag networks = tag.getList(TAG_NETWORKS, NbtType.COMPOUND);
        for (Tag networkTag : networks) {
            parent.addNetwork(convertNetworkFromTag((CompoundTag) networkTag));
        }
    }

    private Network convertNetworkFromTag(CompoundTag networkTag) {
        Set<NetworkNodeReference> refs = convertReferencesFromTag(networkTag);

        NetworkImpl network = new NetworkImpl(networkTag.getUuid(TAG_NETWORK_ID));
        network.getNodeReferences().addAll(refs);

        initializeReferences(refs, network);

        network.onNodesChanged();

        return network;
    }

    private void initializeReferences(Set<NetworkNodeReference> refs, NetworkImpl network) {
        refs.forEach(ref -> ref.get().ifPresent(node -> node.setNetwork(network)));
    }

    private Set<NetworkNodeReference> convertReferencesFromTag(CompoundTag networkTag) {
        Set<NetworkNodeReference> refs = new HashSet<>();
        ListTag refList = networkTag.getList(TAG_NETWORK_NODE_REFERENCES, NbtType.COMPOUND);
        for (Tag refTag : refList) {
            refs.add(FabricNetworkNodeReference.of(server, (CompoundTag) refTag));
        }
        return refs;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        ListTag networks = new ListTag();
        for (Network network : parent.getNetworks()) {
            networks.add(convertNetworkToTag(network));
        }
        tag.put(TAG_NETWORKS, networks);
        return tag;
    }

    private Tag convertNetworkToTag(Network network) {
        CompoundTag tag = new CompoundTag();
        ListTag refs = new ListTag();
        for (NetworkNodeReference ref : network.getNodeReferences()) {
            refs.add(((FabricNetworkNodeReference) ref).toTag());
        }
        tag.putUuid(TAG_NETWORK_ID, network.getId());
        tag.put(TAG_NETWORK_NODE_REFERENCES, refs);
        return tag;
    }
}
