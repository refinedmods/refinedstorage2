package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

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
        final RootStorage fluidRootStorage = network.getComponent(StorageNetworkComponent.class);
        return Platform.INSTANCE.drainContainer(stack)
            .map(extracted -> tryInsert(actor, configuredFluidResource, extracted, fluidRootStorage))
            .map(extracted -> doInsert(actor, extracted, fluidRootStorage));
    }

    @Nullable
    private FluidOperationResult tryInsert(final Actor actor,
                                           final FluidResource configuredResource,
                                           final FluidOperationResult result,
                                           final RootStorage rootStorage) {
        if (!result.fluid().equals(configuredResource)) {
            return null;
        }
        final long insertedSimulated = rootStorage.insert(
            result.fluid(),
            result.amount(),
            Action.SIMULATE,
            actor
        );
        final boolean insertedSuccessfully = insertedSimulated == result.amount();
        return insertedSuccessfully ? result : null;
    }

    private ItemStack doInsert(final Actor actor,
                               final FluidOperationResult result,
                               final RootStorage rootStorage) {
        rootStorage.insert(
            result.fluid(),
            result.amount(),
            Action.EXECUTE,
            actor
        );
        return result.container();
    }
}
