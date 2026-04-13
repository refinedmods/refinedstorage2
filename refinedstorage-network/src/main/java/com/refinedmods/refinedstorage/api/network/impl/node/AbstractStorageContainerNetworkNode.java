package com.refinedmods.refinedstorage.api.network.impl.node;

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

public abstract class AbstractStorageContainerNetworkNode extends AbstractNetworkNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageContainerNetworkNode.class);

    @Nullable
    protected final StateTrackedStorage[] storages;

    private long energyUsage;
    private final long energyUsagePerStorage;

    @Nullable
    private Provider provider;
    private StateTrackedStorage.@Nullable Listener listener;
    private int activeStorages;

    protected AbstractStorageContainerNetworkNode(final long energyUsage,
                                                  final long energyUsagePerStorage,
                                                  final int size) {
        this.energyUsage = energyUsage;
        this.energyUsagePerStorage = energyUsagePerStorage;
        this.storages = new StateTrackedStorage[size];
    }

    public void setListener(final StateTrackedStorage.Listener listener) {
        this.listener = listener;
    }

    public void setProvider(final Provider provider) {
        this.provider = provider;
        final List<StorageChange> changes = new ArrayList<>();
        for (int i = 0; i < storages.length; ++i) {
            changes.addAll(tryUpdateStorage(i));
        }
        LOGGER.info("Set provider for storage container network node, got {} changes", changes.size());
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
                LOGGER.info("Detected storage change at index {}, got {} changes", i, storageChanges.size());
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
            final StateTrackedStorage tracked = new StateTrackedStorage(resolved, listener);
            storages[index] = tracked;
            changes.add(StorageChange.addedAt(index, tracked));
        } else if (resolved == null) {
            storages[index] = null;
            changes.add(StorageChange.removedAt(index, current));
        } else {
            storages[index] = null;
            changes.add(StorageChange.removedAt(index, current));
            final StateTrackedStorage tracked = new StateTrackedStorage(resolved, listener);
            storages[index] = tracked;
            changes.add(StorageChange.addedAt(index, tracked));
        }
        return changes;
    }

    protected void onStorageChange(final StorageChange change) {
        LOGGER.info("Detected storage change: {}", change);
    }

    private void updateActiveStorageCount() {
        this.activeStorages = (int) Arrays.stream(storages).filter(Objects::nonNull).count();
    }

    public void setEnergyUsage(final long energyUsage) {
        this.energyUsage = energyUsage;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage + (energyUsagePerStorage * activeStorages);
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
