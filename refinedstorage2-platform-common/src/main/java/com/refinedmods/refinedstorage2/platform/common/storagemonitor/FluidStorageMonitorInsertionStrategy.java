package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.item.ItemStack;

public class FluidStorageMonitorInsertionStrategy implements StorageMonitorInsertionStrategy {
    @Override
    public Optional<ItemStack> insert(
        final ResourceKey configuredResource,
        final ItemStack stack,
        final Actor actor,
        final Network network
    ) {
        if (!(configuredResource instanceof FluidResource configuredFluidResource)) {
            return Optional.empty();
        }
        final StorageChannel fluidStorageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(StorageChannelTypes.FLUID);
        return Platform.INSTANCE.getContainedFluid(stack)
            .map(extracted -> tryInsert(actor, configuredFluidResource, extracted, fluidStorageChannel))
            .map(extracted -> doInsert(actor, extracted, fluidStorageChannel));
    }

    @Nullable
    private Platform.ContainedFluid tryInsert(final Actor actor,
                                              final FluidResource configuredResource,
                                              final Platform.ContainedFluid result,
                                              final StorageChannel storageChannel) {
        if (!result.fluid().equals(configuredResource)) {
            return null;
        }
        final long insertedSimulated = storageChannel.insert(
            result.fluid(),
            result.amount(),
            Action.SIMULATE,
            actor
        );
        final boolean insertedSuccessfully = insertedSimulated == result.amount();
        return insertedSuccessfully ? result : null;
    }

    private ItemStack doInsert(final Actor actor,
                               final Platform.ContainedFluid extracted,
                               final StorageChannel storageChannel) {
        storageChannel.insert(
            extracted.fluid(),
            extracted.amount(),
            Action.EXECUTE,
            actor
        );
        return extracted.remainderContainer();
    }
}
