package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.TransferHelper;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;

import java.util.Objects;

/**
 * This transfer strategy will import from a {@link SlottedImporterSource}
 * a maximum of the given transfer quota per transfer.
 * It will also only import a single resource type that can be spread out over multiple slots.
 *
 * @param <T> the resource type
 */
public class SlottedImporterTransferStrategy<T> implements ImporterTransferStrategy {
    private final SlottedImporterSource<T> source;
    private final StorageChannel<T> storageChannel;
    private final long transferQuota;

    public SlottedImporterTransferStrategy(final SlottedImporterSource<T> source,
                                           final StorageChannel<T> storageChannel,
                                           final long transferQuota) {
        this.source = source;
        this.storageChannel = storageChannel;
        this.transferQuota = transferQuota;
    }

    @Override
    public void transfer(final Filter filter, final Actor actor) {
        long totalTransferred = 0;
        T workingResource = null;
        for (int i = 0; i < source.getSlots() && totalTransferred < transferQuota; ++i) {
            final T resource = source.getResource(i);
            if (resource == null) {
                continue;
            }
            if (workingResource != null) {
                totalTransferred += performTransfer(actor, totalTransferred, workingResource, i, resource);
            } else {
                final long transferred = performTransfer(actor, totalTransferred, i, resource);
                if (transferred > 0) {
                    workingResource = resource;
                }
                totalTransferred += transferred;
            }
        }
    }

    private long performTransfer(final Actor actor,
                                 final long totalTransferred,
                                 final T workingResource,
                                 final int slot,
                                 final T resourceInSlot) {
        if (Objects.equals(workingResource, resourceInSlot)) {
            return performTransfer(actor, totalTransferred, slot, resourceInSlot);
        }
        return 0L;
    }

    private long performTransfer(final Actor actor,
                                 final long totalTransferred,
                                 final int slot,
                                 final T resourceInSlot) {
        return TransferHelper.transfer(
            resourceInSlot,
            transferQuota - totalTransferred,
            actor,
            new SlottedExtractableStorageWrapper(slot),
            storageChannel
        );
    }

    private class SlottedExtractableStorageWrapper implements ExtractableStorage<T> {
        private final int slot;

        SlottedExtractableStorageWrapper(final int slot) {
            this.slot = slot;
        }

        @Override
        public long extract(final T resource, final long amount, final Action action, final Actor actor) {
            return source.extract(slot, amount, action);
        }
    }
}
