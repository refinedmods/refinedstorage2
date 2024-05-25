package com.refinedmods.refinedstorage2.api.network.impl.node.storagetransfer;

import com.refinedmods.refinedstorage2.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage2.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.filter.Filter;
import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Set;
import java.util.function.ToLongFunction;
import javax.annotation.Nullable;

public class StorageTransferNetworkNode extends AbstractStorageContainerNetworkNode {
    private final Filter filter = new Filter();

    private StorageTransferMode mode = StorageTransferMode.INSERT;
    @Nullable
    private ToLongFunction<Storage> transferQuotaProvider;

    public StorageTransferNetworkNode(final long energyUsage, final long energyUsagePerStorage, final int size) {
        super(energyUsage, energyUsagePerStorage, size);
    }

    public void setMode(final StorageTransferMode mode) {
        this.mode = mode;
    }

    public void setFilters(final Set<ResourceKey> filters) {
        filter.setFilters(filters);
    }

    public void setFilterMode(final FilterMode filterMode) {
        filter.setMode(filterMode);
    }

    public void setTransferQuotaProvider(final ToLongFunction<Storage> transferQuotaProvider) {
        this.transferQuotaProvider = transferQuotaProvider;
    }

    @Override
    public void doWork() {
        super.doWork();
        if (!isActive() || network == null) {
            return;
        }
        final StorageNetworkComponent storage = network.getComponent(StorageNetworkComponent.class);
        switch (mode) {
            case INSERT -> doInsert();
            case EXTRACT -> {
            }
        }
    }

    private void doInsert() {
        for (final Storage storage : storages) {
            if (storage != null && doInsert(storage)) {
                return;
            }
        }
    }

    private boolean doInsert(final Storage storage) {
        if (transferQuotaProvider == null) {
            return false;
        }
        final long transferQuota = transferQuotaProvider.applyAsLong(storage);
        for (final ResourceAmount resourceAmount : storage.getAll()) {
            final ResourceKey resource = resourceAmount.getResource();
            final long amount = resourceAmount.getAmount();

        }
        return false;
    }
}
