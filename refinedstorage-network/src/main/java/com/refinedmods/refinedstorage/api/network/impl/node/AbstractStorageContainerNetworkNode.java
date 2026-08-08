package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetailsProvider;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeType;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStorageContainerNetworkNode extends AbstractNetworkNode
    implements NetworkNodeDetailsProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageContainerNetworkNode.class);

    @Nullable
    protected final StateTrackedStorage[] storages;

    private final NetworkNodeType type;
    private final NetworkNodeEventManager eventManager = new NetworkNodeEventManager();
    private final long energyUsagePerStorage;

    @Nullable
    private Provider provider;
    private StateTrackedStorage.@Nullable Listener storageListener;
    private int activeStorages;
    private long baseEnergyUsage;

    protected AbstractStorageContainerNetworkNode(final NetworkNodeType type,
                                                  final long baseEnergyUsage,
                                                  final long energyUsagePerStorage,
                                                  final int size) {
        this.type = type;
        this.baseEnergyUsage = baseEnergyUsage;
        this.energyUsagePerStorage = energyUsagePerStorage;
        this.storages = new StateTrackedStorage[size];
    }

    public void setStorageListener(final StateTrackedStorage.Listener storageListener) {
        this.storageListener = storageListener;
    }

    public void setBaseEnergyUsage(final long baseEnergyUsage) {
        this.baseEnergyUsage = baseEnergyUsage;
        eventManager.notifyDetailsChanged(getEnergyUsage(), isActive());
    }

    public void setProvider(final Provider provider) {
        this.provider = provider;
        final List<StorageChange> changes = new ArrayList<>();
        for (int i = 0; i < storages.length; ++i) {
            changes.addAll(tryUpdateStorage(i));
        }
        LOGGER.debug("Set provider for storage container network node, got {} changes", changes.size());
        // If we are already initialized, update all the storages to keep the exposed storages in sync.
        // If we are not initialized, update nothing as we have to wait for an activeness update.
        if (activeStorages > 0) {
            changes.forEach(this::onStorageChange);
        }
        updateActiveStorageCount();
    }

    public void onStorageChanged() {
        for (int i = 0; i < storages.length; ++i) {
            final Set<StorageChange> storageChanges = tryUpdateStorage(i);
            if (!storageChanges.isEmpty()) {
                LOGGER.debug("Detected storage change at index {}, got {} changes", i, storageChanges.size());
            }
            storageChanges.forEach(this::onStorageChange);
        }
        updateActiveStorageCount();
    }

    private Set<StorageChange> tryUpdateStorage(final int index) {
        final Set<StorageChange> changes = new HashSet<>();
        final StateTrackedStorage current = storages[index];
        final Storage resolved = provider != null ? provider.resolve(index).orElse(null) : null;
        if (current == null && resolved == null) {
            return Collections.emptySet();
        }
        if (current != null && current.getDelegate() == resolved) {
            return Collections.emptySet();
        }
        if (current == null) {
            final StateTrackedStorage tracked = new StateTrackedStorage(resolved, storageListener);
            storages[index] = tracked;
            changes.add(StorageChange.addedAt(index, tracked));
        } else if (resolved == null) {
            storages[index] = null;
            changes.add(StorageChange.removedAt(index, current));
        } else {
            storages[index] = null;
            changes.add(StorageChange.removedAt(index, current));
            final StateTrackedStorage tracked = new StateTrackedStorage(resolved, storageListener);
            storages[index] = tracked;
            changes.add(StorageChange.addedAt(index, tracked));
        }
        return changes;
    }

    protected void onStorageChange(final StorageChange change) {
        LOGGER.debug("Detected storage change: {}", change);
    }

    private void updateActiveStorageCount() {
        this.activeStorages = (int) Arrays.stream(storages).filter(Objects::nonNull).count();
        eventManager.notifyDetailsChanged(getEnergyUsage(), isActive());
    }

    @Override
    public long getEnergyUsage() {
        return baseEnergyUsage + (energyUsagePerStorage * activeStorages);
    }

    public int getSize() {
        return storages.length;
    }

    public StorageState getState(final int index) {
        final var storage = storages[index];
        if (storage == null) {
            return StorageState.NONE;
        }
        if (!isActive()) {
            return StorageState.INACTIVE;
        }
        return storage.getState();
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        eventManager.notifyDetailsChanged(getEnergyUsage(), newActive);
    }

    @Override
    public NetworkNodeType getType() {
        return type;
    }

    @Override
    public NetworkNodeDetails createDetails() {
        return SimpleNetworkNodeDetails.of(this);
    }

    @Override
    public void addListener(final NetworkNodeListener listener) {
        eventManager.addListener(listener);
    }

    @Override
    public void removeListener(final NetworkNodeListener listener) {
        eventManager.removeListener(listener);
    }

    protected record StorageChange(int index, boolean removed, StateTrackedStorage storage) {
        private static StorageChange removedAt(final int index, final StateTrackedStorage storage) {
            return new StorageChange(index, true, storage);
        }

        private static StorageChange addedAt(final int index, final StateTrackedStorage storage) {
            return new StorageChange(index, false, storage);
        }
    }

    @FunctionalInterface
    public interface Provider {
        Optional<Storage> resolve(int index);
    }
}
