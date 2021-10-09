package com.refinedmods.refinedstorage2.api.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.filter.Filter;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;

import java.util.Optional;

public class DiskDriveItemStorage extends DiskDriveStorage<Rs2ItemStack> {
    private final Filter<Rs2ItemStack> itemFilter;

    public DiskDriveItemStorage(DiskDriveNetworkNode diskDrive, Filter<Rs2ItemStack> itemFilter) {
        super(diskDrive, StorageChannelTypes.ITEM);
        this.itemFilter = itemFilter;
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack resource, long amount, Action action) {
        if (!itemFilter.isAllowed(resource)) {
            return notAllowed(resource, amount);
        }
        return super.insert(resource, amount, action);
    }
}
