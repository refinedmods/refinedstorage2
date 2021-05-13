package com.refinedmods.refinedstorage2.core.network;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReference;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReferencingEnergyStorage;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeReferencingStorage;
import com.refinedmods.refinedstorage2.core.storage.EmptyItemStorage;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.StorageChannel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;

public class NetworkImpl implements Network {
    private final UUID id;
    private final Set<NetworkNodeReference> nodeReferences = new HashSet<>();
    private final ItemStorageChannel itemStorageChannel = new ItemStorageChannel();
    private final CompositeEnergyStorage energyStorage = new CompositeEnergyStorage();

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
    public void onNodesChanged() {
        invalidateStorageChannelSources();
        invalidateEnergySources();
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private void invalidateEnergySources() {
        List<EnergyStorage> sources = mapReferences(
                EnergyStorage.class::isInstance,
                NetworkNodeReferencingEnergyStorage::new
        );
        energyStorage.setSources(sources);
    }

    @Override
    public void invalidateStorageChannelSources() {
        List<Storage<Rs2ItemStack>> sources = mapReferences(
                Storage.class::isInstance,
                ref -> new NetworkNodeReferencingStorage<>(ref, new EmptyItemStorage())
        );
        itemStorageChannel.setSources(sources);
    }

    private <T> List<T> mapReferences(Predicate<NetworkNode> filter, Function<NetworkNodeReference, T> transformer) {
        List<T> result = new ArrayList<>();
        for (NetworkNodeReference ref : nodeReferences) {
            Optional<NetworkNode> node = ref.get();
            if (node.isPresent() && filter.test(node.get())) {
                result.add(transformer.apply(ref));
            }
        }
        return result;
    }

    @Override
    public StorageChannel<Rs2ItemStack> getItemStorageChannel() {
        return itemStorageChannel;
    }
}
