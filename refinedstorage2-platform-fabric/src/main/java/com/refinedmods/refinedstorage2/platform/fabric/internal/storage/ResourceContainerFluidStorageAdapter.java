package com.refinedmods.refinedstorage2.platform.fabric.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.ofFluidVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.util.VariantUtil.toFluidVariant;

public class ResourceContainerFluidStorageAdapter extends SnapshotParticipant<ResourceContainer>
    implements Storage<FluidVariant> {
    private final ResourceContainer resourceContainer;

    public ResourceContainerFluidStorageAdapter(final ResourceContainer resourceContainer) {
        this.resourceContainer = resourceContainer;
    }

    @Override
    public long insert(final FluidVariant fluidVariant, final long maxAmount, final TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        final FluidResource fluidResource = ofFluidVariant(fluidVariant);
        final long insertedSimulated = resourceContainer.insert(
            StorageChannelTypes.FLUID,
            fluidResource,
            maxAmount,
            Action.SIMULATE
        );
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return resourceContainer.insert(
            StorageChannelTypes.FLUID,
            fluidResource,
            maxAmount,
            Action.EXECUTE
        );
    }

    @Override
    public long extract(final FluidVariant fluidVariant, final long maxAmount, final TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        final FluidResource fluidResource = ofFluidVariant(fluidVariant);
        final long extractedSimulated = resourceContainer.extract(fluidResource, maxAmount, Action.SIMULATE);
        if (extractedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return resourceContainer.extract(fluidResource, maxAmount, Action.EXECUTE);
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        final List<StorageView<FluidVariant>> list = new ArrayList<>();
        for (int i = 0; i < resourceContainer.size(); ++i) {
            list.add(new StorageViewImpl(i));
        }
        return list.iterator();
    }

    @Override
    protected ResourceContainer createSnapshot() {
        return resourceContainer.copy();
    }

    @Override
    protected void readSnapshot(final ResourceContainer snapshot) {
        for (int i = 0; i < snapshot.size(); ++i) {
            final ResourceAmountTemplate<?> snapshotSlot = snapshot.get(i);
            if (snapshotSlot == null) {
                resourceContainer.remove(i);
            } else {
                resourceContainer.set(i, snapshotSlot);
            }
        }
    }

    private class StorageViewImpl implements StorageView<FluidVariant> {
        private final int index;

        private StorageViewImpl(final int index) {
            this.index = index;
        }

        @Override
        public long extract(final FluidVariant resource, final long maxAmount, final TransactionContext transaction) {
            final ResourceAmountTemplate<?> slot = resourceContainer.get(index);
            if (slot == null
                || !(slot.getResource() instanceof FluidResource fluidResource)
                || !resource.equals(toFluidVariant(fluidResource))) {
                return 0;
            }
            final long extracted = Math.min(maxAmount, slot.getAmount());
            if (extracted > 0) {
                updateSnapshots(transaction);
            }
            resourceContainer.shrink(index, extracted);
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return resourceContainer.get(index) == null;
        }

        @Override
        public FluidVariant getResource() {
            final ResourceAmountTemplate<?> slot = resourceContainer.get(index);
            if (slot == null || !(slot.getResource() instanceof FluidResource fluidResource)) {
                return FluidVariant.blank();
            }
            return toFluidVariant(fluidResource);
        }

        @Override
        public long getAmount() {
            final ResourceAmountTemplate<?> slot = resourceContainer.get(index);
            if (slot == null) {
                return 0;
            }
            return slot.getAmount();
        }

        @Override
        public long getCapacity() {
            final ResourceAmountTemplate<?> slot = resourceContainer.get(index);
            if (slot == null) {
                return 0;
            }
            return resourceContainer.getMaxAmount(slot);
        }
    }
}
