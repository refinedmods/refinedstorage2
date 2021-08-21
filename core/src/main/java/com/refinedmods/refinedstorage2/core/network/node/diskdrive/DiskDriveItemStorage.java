package com.refinedmods.refinedstorage2.core.network.node.diskdrive;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.core.util.Filter;

import java.util.Optional;

public class DiskDriveItemStorage extends DiskDriveStorage<Rs2ItemStack> {
    private final Filter<Rs2ItemStack> itemFilter;

    public DiskDriveItemStorage(DiskDriveNetworkNode diskDrive, Filter<Rs2ItemStack> itemFilter) {
        super(diskDrive, StorageChannelTypes.ITEM);
        this.itemFilter = itemFilter;
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        if (!itemFilter.isAllowed(template)) {
            return notAllowed(template, amount);
        }
        return super.insert(template, amount, action);
    }
}
