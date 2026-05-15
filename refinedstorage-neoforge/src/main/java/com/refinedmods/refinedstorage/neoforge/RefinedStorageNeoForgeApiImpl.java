package com.refinedmods.refinedstorage.neoforge;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.neoforge.api.RefinedStorageNeoForgeApi;
import com.refinedmods.refinedstorage.neoforge.api.ResourceHandlerExternalPatternSinkStrategyFactory;
import com.refinedmods.refinedstorage.neoforge.autocrafting.ResourceHandlerPatternProviderExternalPatternSinkFactory;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class RefinedStorageNeoForgeApiImpl implements RefinedStorageNeoForgeApi {
    private final BlockCapability<NetworkNodeContainerProvider, @Nullable Direction>
        networkNodeContainerProviderCapability = BlockCapability.create(
        createIdentifier("network_node_container_provider"),
        NetworkNodeContainerProvider.class,
        Direction.class
    );

    @Override
    public BlockCapability<NetworkNodeContainerProvider,
        @Nullable Direction> getNetworkNodeContainerProviderCapability() {
        return networkNodeContainerProviderCapability;
    }

    @Override
    public void addResourceHandlerExternalPatternSinkStrategyFactory(
        final Class<? extends ResourceKey> resourceType,
        final ResourceHandlerExternalPatternSinkStrategyFactory factory
    ) {
        ((ResourceHandlerPatternProviderExternalPatternSinkFactory)
            Platform.INSTANCE.getPatternProviderExternalPatternSinkFactory()).addFactory(resourceType, factory);
    }
}
