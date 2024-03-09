package com.refinedmods.refinedstorage2.platform.fabric.support.resource;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.support.resource.FluidResource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.ofFluidVariant;
import static com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil.toFluidVariant;

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
        final long insertedSimulated = resourceContainer.insert(fluidResource, maxAmount, Action.SIMULATE);
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return resourceContainer.insert(fluidResource, maxAmount, Action.EXECUTE);
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
            final ResourceAmount snapshotSlot = snapshot.get(i);
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
            final ResourceAmount resourceAmount = resourceContainer.get(index);
            if (resourceAmount == null
                || !(resourceAmount.getResource() instanceof FluidResource fluidResource)
                || !resource.equals(toFluidVariant(fluidResource))) {
                return 0;
            }
            final long extracted = Math.min(maxAmount, resourceAmount.getAmount());
            if (extracted > 0) {
                updateSnapshots(transaction);
            }
            resourceContainer.shrink(index, extracted);
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return resourceContainer.isEmpty(index);
        }

        @Override
        public FluidVariant getResource() {
            final PlatformResourceKey resource = resourceContainer.getResource(index);
            if (!(resource instanceof FluidResource fluidResource)) {
                return FluidVariant.blank();
            }
            return toFluidVariant(fluidResource);
        }

        @Override
        public long getAmount() {
            return resourceContainer.getAmount(index);
        }

        @Override
        public long getCapacity() {
            final ResourceKey resource = resourceContainer.getResource(index);
            if (resource == null) {
                return 0;
            }
            return resourceContainer.getMaxAmount(resource);
        }
    }
}
