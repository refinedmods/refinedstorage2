package com.refinedmods.refinedstorage.fabric.api;

import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class RefinedStorageFabricApiProxy implements RefinedStorageFabricApi {
    @Nullable
    private RefinedStorageFabricApi delegate;

    public void setDelegate(final RefinedStorageFabricApi delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("Fabric API already injected");
        }
        this.delegate = delegate;
    }

    private RefinedStorageFabricApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("Fabric API not loaded yet");
        }
        return delegate;
    }

    @Override
    public BlockApiLookup<NetworkNodeContainerProvider, @Nullable Direction> getNetworkNodeContainerProviderLookup() {
        return ensureLoaded().getNetworkNodeContainerProviderLookup();
    }

    @Override
    public void addStorageExternalPatternSinkStrategyFactory(
        final StorageExternalPatternSinkStrategyFactory factory) {
        ensureLoaded().addStorageExternalPatternSinkStrategyFactory(factory);
    }
}
