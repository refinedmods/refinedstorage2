package com.refinedmods.refinedstorage.fabric.support.resource;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.ofFluidVariant;
import static com.refinedmods.refinedstorage.fabric.support.resource.VariantUtil.toFluidVariant;

public class ResourceContainerFluidStorageAdapter extends SnapshotParticipant<ResourceContainer>
    implements Storage<FluidVariant> {
    private final ResourceContainer container;

    public ResourceContainerFluidStorageAdapter(final ResourceContainer container) {
        this.container = container;
    }

    @Override
    public long insert(final FluidVariant fluidVariant, final long maxAmount, final TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        final FluidResource fluidResource = ofFluidVariant(fluidVariant);
        final long insertedSimulated = container.insert(fluidResource, maxAmount, Action.SIMULATE);
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return container.insert(fluidResource, maxAmount, Action.EXECUTE);
    }

    @Override
    public long extract(final FluidVariant fluidVariant, final long maxAmount, final TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(fluidVariant, maxAmount);
        final FluidResource fluidResource = ofFluidVariant(fluidVariant);
        final long extractedSimulated = container.extract(fluidResource, maxAmount, Action.SIMULATE);
        if (extractedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return container.extract(fluidResource, maxAmount, Action.EXECUTE);
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        final List<StorageView<FluidVariant>> list = new ArrayList<>();
        for (int i = 0; i < container.size(); ++i) {
            list.add(new StorageViewImpl(i));
        }
        return list.iterator();
    }

    @Override
    protected ResourceContainer createSnapshot() {
        return container.copy();
    }

    @Override
    protected void readSnapshot(final ResourceContainer snapshot) {
        for (int i = 0; i < snapshot.size(); ++i) {
            final ResourceAmount snapshotSlot = snapshot.get(i);
            if (snapshotSlot == null) {
                container.remove(i);
            } else {
                container.set(i, snapshotSlot);
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
            final ResourceAmount resourceAmount = container.get(index);
            if (resourceAmount == null
                || !(resourceAmount.resource() instanceof FluidResource fluidResource)
                || !resource.equals(toFluidVariant(fluidResource))) {
                return 0;
            }
            final long extracted = Math.min(maxAmount, resourceAmount.amount());
            if (extracted > 0) {
                updateSnapshots(transaction);
            }
            container.shrink(index, extracted);
            return extracted;
        }

        @Override
        public boolean isResourceBlank() {
            return container.isEmpty(index);
        }

        @Override
        public FluidVariant getResource() {
            final PlatformResourceKey resource = container.getResource(index);
            if (!(resource instanceof FluidResource fluidResource)) {
                return FluidVariant.blank();
            }
            return toFluidVariant(fluidResource);
        }

        @Override
        public long getAmount() {
            return container.getAmount(index);
        }

        @Override
        public long getCapacity() {
            final ResourceKey resource = container.getResource(index);
            if (resource == null) {
                return 0;
            }
            return container.getMaxAmount(resource);
        }
    }
}
