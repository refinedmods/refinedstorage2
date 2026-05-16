package com.refinedmods.refinedstorage.fabric;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.fabric.api.RefinedStorageFabricApi;
import com.refinedmods.refinedstorage.fabric.api.StorageExternalPatternSinkStrategyFactory;
import com.refinedmods.refinedstorage.fabric.autocrafting.StoragePatternProviderExternalPatternSinkFactory;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class RefinedStorageFabricApiImpl implements RefinedStorageFabricApi {
    private final BlockApiLookup<NetworkNodeContainerProvider, @Nullable Direction> networkNodeContainerProvider =
        BlockApiLookup.get(
            createIdentifier("network_node_container_provider"),
            NetworkNodeContainerProvider.class,
            Direction.class
        );

    @Override
    public BlockApiLookup<NetworkNodeContainerProvider, @Nullable Direction> getNetworkNodeContainerProviderLookup() {
        return networkNodeContainerProvider;
    }

    @Override
    public void addStorageExternalPatternSinkStrategyFactory(final Class<? extends ResourceKey> resourceType,
                                                             final StorageExternalPatternSinkStrategyFactory factory) {
        ((StoragePatternProviderExternalPatternSinkFactory)
            Platform.INSTANCE.getPatternProviderExternalPatternSinkFactory()).addFactory(resourceType, factory);
    }
}
