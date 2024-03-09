package com.refinedmods.refinedstorage2.api.network.impl.node.multistorage;

import com.refinedmods.refinedstorage2.api.network.component.StorageProvider;
import com.refinedmods.refinedstorage2.api.network.node.AbstractStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiStorageNetworkNode extends AbstractStorageNetworkNode implements StorageProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiStorageNetworkNode.class);

    @Nullable
    private MultiStorageProvider provider;
    @Nullable
    private StateTrackedStorage.Listener listener;

    private final long energyUsage;
    private final long energyUsagePerStorage;

    private final StateTrackedStorage[] cache;
    private final ExposedMultiStorage storage;
    private int activeStorages;

    public MultiStorageNetworkNode(final long energyUsage,
                                   final long energyUsagePerStorage,
                                   final int size) {
        this.energyUsage = energyUsage;
        this.energyUsagePerStorage = energyUsagePerStorage;
        this.storage = new ExposedMultiStorage(this);
        this.cache = new StateTrackedStorage[size];
    }

    public void setProvider(final MultiStorageProvider provider) {
        this.provider = provider;
        final List<StorageChange> changes = new ArrayList<>();
        for (int i = 0; i < cache.length; ++i) {
            changes.addAll(initializeStorage(i));
        }
        // If we are already initialized, update all the storages to keep the exposed storages in sync.
        // If we are not initialized, update nothing as we have to wait for an activeness update.
        if (activeStorages > 0) {
            changes.forEach(this::processStorageChange);
        }
        updateActiveStorageCount();
    }

    public void onStorageChanged(final int index) {
        if (index < 0 || index >= cache.length) {
            LOGGER.warn("Invalid index {}", index);
            return;
        }
        initializeStorage(index).forEach(this::processStorageChange);
        updateActiveStorageCount();
    }

    private Set<StorageChange> initializeStorage(final int index) {
        final Set<StorageChange> results = new HashSet<>();

        if (cache[index] != null) {
            results.add(new StorageChange(true, cache[index]));
        }

        if (provider != null) {
            provider.resolve(index).ifPresentOrElse(resolved -> {
                final StateTrackedStorage newStorage = new StateTrackedStorage(resolved, listener);
                cache[index] = newStorage;
                results.add(new StorageChange(false, newStorage));
            }, () -> cache[index] = null);
        }

        return results;
    }

    private void processStorageChange(final StorageChange change) {
        if (!isActive()) {
            return;
        }
        if (change.removed) {
            storage.removeSource(change.storage);
        } else {
            storage.addSource(change.storage);
        }
    }

    private void updateActiveStorageCount() {
        this.activeStorages = (int) Arrays.stream(cache).filter(Objects::nonNull).count();
    }

    @Override
    protected void onActiveChanged(final boolean newActive) {
        super.onActiveChanged(newActive);
        if (network == null) {
            return;
        }
        LOGGER.debug("Activeness got changed to {}, updating underlying internal storages", newActive);
        if (newActive) {
            enableAllStorages();
        } else {
            disableAllStorages();
        }
    }

    private void enableAllStorages() {
        for (final StateTrackedStorage internalStorage : cache) {
            if (internalStorage != null) {
                storage.addSource(internalStorage);
            }
        }
    }

    private void disableAllStorages() {
        storage.clearSources();
    }

    public void setListener(final StateTrackedStorage.Listener listener) {
        this.listener = listener;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsage + (energyUsagePerStorage * activeStorages);
    }

    public int getSize() {
        return cache.length;
    }

    public StorageState getState(final int index) {
        final var cached = cache[index];
        if (cached == null) {
            return StorageState.NONE;
        }
        if (!isActive()) {
            return StorageState.INACTIVE;
        }
        return cached.getState();
    }

    @Override
    public Storage getStorage() {
        return storage;
    }

    private record StorageChange(boolean removed, StateTrackedStorage storage) {
    }
}
