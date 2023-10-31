package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.ContainedFluid;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public class FluidStorageMonitorInsertionStrategy implements StorageMonitorInsertionStrategy {
    @Override
    public Optional<ItemStack> insert(
        final Object configuredResource,
        final ItemStack stack,
        final Actor actor,
        final Network network
    ) {
        if (!(configuredResource instanceof FluidResource configuredFluidResource)) {
            return Optional.empty();
        }
        final StorageChannel<FluidResource> fluidStorageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.FLUID);
        return Platform.INSTANCE.getContainedFluid(stack)
            .map(extracted -> tryInsert(actor, configuredFluidResource, extracted, fluidStorageChannel))
            .map(extracted -> doInsert(actor, extracted, fluidStorageChannel));
    }

    @Nullable
    private ContainedFluid tryInsert(final Actor actor,
                                     final FluidResource configuredResource,
                                     final ContainedFluid result,
                                     final StorageChannel<FluidResource> storageChannel) {
        final ResourceAmount<FluidResource> fluid = result.fluid();
        if (!fluid.getResource().equals(configuredResource)) {
            return null;
        }
        final long insertedSimulated = storageChannel.insert(
            fluid.getResource(),
            fluid.getAmount(),
            Action.SIMULATE,
            actor
        );
        final boolean insertedSuccessfully = insertedSimulated == fluid.getAmount();
        return insertedSuccessfully ? result : null;
    }

    private ItemStack doInsert(final Actor actor,
                               final ContainedFluid extracted,
                               final StorageChannel<FluidResource> storageChannel) {
        final ResourceAmount<FluidResource> fluid = extracted.fluid();
        storageChannel.insert(
            fluid.getResource(),
            fluid.getAmount(),
            Action.EXECUTE,
            actor
        );
        return extracted.remainderContainer();
    }
}
