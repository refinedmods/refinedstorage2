package com.refinedmods.refinedstorage2.core.network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReferencingStorage;
import com.refinedmods.refinedstorage2.core.storage.EmptyItemStorage;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;
import net.minecraft.item.ItemStack;

public class NetworkImpl implements Network {
    private final UUID id;
    private final Set<NetworkNodeReference> nodeReferences = new HashSet<>();
    private final ItemStorageChannel itemStorageChannel = new ItemStorageChannel();

    public NetworkImpl(UUID id) {
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Set<NetworkNodeReference> getNodeReferences() {
        return nodeReferences;
    }

    @Override
    public void invalidateStorageChannelSources() {
        itemStorageChannel.setSources(createStorageSources());
    }

    private List<Storage<ItemStack>> createStorageSources() {
        List<Storage<ItemStack>> sources = new ArrayList<>();
        for (NetworkNodeReference ref : nodeReferences) {
            Optional<NetworkNode> node = ref.get();
            if (node.isPresent() && node.get() instanceof Storage) {
                sources.add(new NetworkNodeReferencingStorage<>(ref, new EmptyItemStorage()));
            }
        }

        return sources;
    }

    @Override
    public StorageChannel<ItemStack> getItemStorageChannel() {
        return itemStorageChannel;
    }
}
