package com.refinedmods.refinedstorage.api.network.impl.node;

import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStorageContainerNetworkNode extends AbstractNetworkNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorageContainerNetworkNode.class);

    protected final StateTrackedStorage[] storages;

    private long energyUsage;
    private final long energyUsagePerStorage;

    @Nullable
    private Provider provider;
    @Nullable
    private StateTrackedStorage.Listener listener;
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
            changes.addAll(initializeStorage(i));
        }
        // If we are already initialized, update all the storages to keep the exposed storages in sync.
        // If we are not initialized, update nothing as we have to wait for an activeness update.
        if (activeStorages > 0) {
            changes.forEach(this::onStorageChange);
        }
        updateActiveStorageCount();
    }

    public void onStorageChanged(final int index) {
        if (index < 0 || index >= storages.length) {
            LOGGER.warn("Invalid index {}", index);
            return;
        }
        initializeStorage(index).forEach(this::onStorageChange);
        updateActiveStorageCount();
    }

    protected void onStorageChange(final StorageChange change) {
        // no op
    }

    private Set<StorageChange> initializeStorage(final int index) {
        final Set<StorageChange> results = new HashSet<>();
        if (storages[index] != null) {
            results.add(new StorageChange(true, storages[index]));
        }
        if (provider != null) {
            provider.resolve(index).ifPresentOrElse(resolved -> {
                final StateTrackedStorage newStorage = new StateTrackedStorage(resolved, listener);
                storages[index] = newStorage;
                results.add(new StorageChange(false, newStorage));
            }, () -> storages[index] = null);
        }
        return results;
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

    protected record StorageChange(boolean removed, StateTrackedStorage storage) {
    }

    @FunctionalInterface
    public interface Provider {
        Optional<Storage> resolve(int index);
    }
}
