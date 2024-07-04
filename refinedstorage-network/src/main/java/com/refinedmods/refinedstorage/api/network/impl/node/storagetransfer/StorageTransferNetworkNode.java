package com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.TransferHelper;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

public class StorageTransferNetworkNode extends AbstractStorageContainerNetworkNode {
    private final Actor actor = new NetworkNodeActor(this);
    private final Filter filter = new Filter();

    private StorageTransferMode mode = StorageTransferMode.INSERT_INTO_NETWORK;
    @Nullable
    private ToLongFunction<Storage> transferQuotaProvider;
    @Nullable
    private StorageTransferListener listener;

    public StorageTransferNetworkNode(final long energyUsage, final long energyUsagePerStorage, final int size) {
        super(energyUsage, energyUsagePerStorage, size);
    }

    public void setMode(final StorageTransferMode mode) {
        this.mode = mode;
    }

    public StorageTransferMode getMode() {
        return mode;
    }

    public void setTransferQuotaProvider(final ToLongFunction<Storage> transferQuotaProvider) {
        this.transferQuotaProvider = transferQuotaProvider;
    }

    public void setListener(@Nullable final StorageTransferListener listener) {
        this.listener = listener;
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(final FilterMode filterMode) {
        filter.setMode(filterMode);
    }

    public void setFilters(final Set<ResourceKey> filters) {
        filter.setFilters(filters);
    }

    public void setNormalizer(final UnaryOperator<ResourceKey> normalizer) {
        filter.setNormalizer(normalizer);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (!isActive() || network == null) {
            return;
        }
        final StorageNetworkComponent networkStorage = network.getComponent(StorageNetworkComponent.class);
        for (int i = 0; i < storages.length / 2; ++i) {
            final StateTrackedStorage storage = storages[i];
            if (storage == null) {
                continue;
            }
            final Result result = transfer(storage, networkStorage);
            if (processResult(result, i)) {
                return;
            }
        }
    }

    private Result transfer(final StateTrackedStorage storage, final StorageNetworkComponent networkStorage) {
        if (transferQuotaProvider == null) {
            return Result.FAILURE;
        }
        final long transferQuota = transferQuotaProvider.applyAsLong(storage.getDelegate());
        if (mode == StorageTransferMode.INSERT_INTO_NETWORK) {
            return transfer(storage, networkStorage, transferQuota, this::hasNoExtractableResources);
        }
        return transfer(
            networkStorage,
            storage,
            transferQuota,
            source -> hasNoExtractableResources(source) || storageIsFull(storage)
        );
    }

    private Result transfer(final Storage source,
                            final Storage destination,
                            final long transferQuota,
                            final Predicate<Storage> readyPredicate) {
        if (readyPredicate.test(source)) {
            return Result.SUCCESS;
        }
        if (transfer(source, destination, transferQuota)) {
            return readyPredicate.test(source)
                ? Result.SUCCESS
                : Result.PARTIAL;
        }
        return Result.FAILURE;
    }

    private boolean transfer(final Storage source, final Storage destination, final long transferQuota) {
        long remainder = transferQuota;
        final Collection<ResourceAmount> sourceContents = new LinkedHashSet<>(source.getAll());
        for (final ResourceAmount resourceAmount : sourceContents) {
            final ResourceKey resource = resourceAmount.getResource();
            if (!filter.isAllowed(resource)) {
                continue;
            }
            final long amount = Math.min(remainder, resourceAmount.getAmount());
            final long transferred = TransferHelper.transfer(resource, amount, actor, source, destination, source);
            remainder -= transferred;
            if (remainder == 0) {
                return true;
            }
        }
        return remainder != transferQuota;
    }

    private boolean hasNoExtractableResources(final Storage source) {
        return source.getAll().stream().noneMatch(resourceAmount -> filter.isAllowed(resourceAmount.getResource()));
    }

    private boolean storageIsFull(final Storage storage) {
        return storage instanceof LimitedStorage limitedStorage
            && limitedStorage.getCapacity() > 0
            && limitedStorage.getCapacity() == limitedStorage.getStored();
    }

    private boolean processResult(final Result result, final int index) {
        if (result.isSuccess()) {
            if (result == Result.SUCCESS && listener != null) {
                listener.onTransferSuccess(index);
            }
            return true;
        }
        return false;
    }

    private enum Result {
        SUCCESS,
        PARTIAL,
        FAILURE;

        private boolean isSuccess() {
            return this == PARTIAL || this == SUCCESS;
        }
    }
}
