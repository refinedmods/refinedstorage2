package com.refinedmods.refinedstorage.neoforge.support.resource;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;

import java.util.Objects;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.ofPlatform;
import static com.refinedmods.refinedstorage.neoforge.support.resource.VariantUtil.toPlatform;

public class ResourceContainerResourceHandlerAdapter
    extends SnapshotJournal<ResourceContainer>
    implements ResourceHandler<net.neoforged.neoforge.transfer.fluid.FluidResource> {
    private final ResourceContainer container;

    public ResourceContainerResourceHandlerAdapter(final ResourceContainer container) {
        this.container = container;
    }

    @Override
    public int size() {
        return container.size();
    }

    @Override
    public net.neoforged.neoforge.transfer.fluid.FluidResource getResource(final int index) {
        final ResourceAmount resourceAmount = container.get(index);
        if (resourceAmount == null || !(resourceAmount.resource() instanceof FluidResource fluidResource)) {
            return net.neoforged.neoforge.transfer.fluid.FluidResource.EMPTY;
        }
        return toPlatform(fluidResource);
    }

    @Override
    public long getAmountAsLong(final int index) {
        final ResourceAmount resourceAmount = container.get(index);
        if (resourceAmount == null || !(resourceAmount.resource() instanceof FluidResource)) {
            return 0;
        }
        return resourceAmount.amount();
    }

    @Override
    public long getCapacityAsLong(final int index, final net.neoforged.neoforge.transfer.fluid.FluidResource resource) {
        return ResourceTypes.FLUID.getInterfaceExportLimit();
    }

    @Override
    public boolean isValid(final int index, final net.neoforged.neoforge.transfer.fluid.FluidResource resource) {
        return true;
    }

    @Override
    public int insert(final int index, final net.neoforged.neoforge.transfer.fluid.FluidResource resource,
                      final int amount, final TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        final FluidResource fluidResource = ofPlatform(resource);
        final long insertedSimulated = container.insert(fluidResource, amount, Action.SIMULATE);
        if (insertedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return (int) container.insert(fluidResource, amount, Action.EXECUTE);
    }

    @Override
    public int extract(final int index, final net.neoforged.neoforge.transfer.fluid.FluidResource resource,
                       final int amount, final TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        final FluidResource fluidResource = ofPlatform(resource);
        final long extractedSimulated = container.extract(fluidResource, amount, Action.SIMULATE);
        if (extractedSimulated > 0) {
            updateSnapshots(transaction);
        }
        return (int) container.extract(fluidResource, amount, Action.EXECUTE);
    }

    @Override
    protected ResourceContainer createSnapshot() {
        return container.copy();
    }

    @Override
    protected void revertToSnapshot(final ResourceContainer snapshot) {
        for (int i = 0; i < snapshot.size(); ++i) {
            final ResourceAmount snapshotSlot = snapshot.get(i);
            if (snapshotSlot == null) {
                container.remove(i);
            } else {
                container.set(i, snapshotSlot);
            }
        }
    }
}
