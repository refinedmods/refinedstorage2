package com.refinedmods.refinedstorage.neoforge.api;

import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

public class RefinedStorageNeoForgeApiProxy implements RefinedStorageNeoForgeApi {
    @Nullable
    private RefinedStorageNeoForgeApi delegate;

    public void setDelegate(final RefinedStorageNeoForgeApi delegate) {
        if (this.delegate != null) {
            throw new IllegalStateException("NeoForge API already injected");
        }
        this.delegate = delegate;
    }

    @Override
    public BlockCapability<NetworkNodeContainerProvider,
        @Nullable Direction> getNetworkNodeContainerProviderCapability() {
        return ensureLoaded().getNetworkNodeContainerProviderCapability();
    }

    @Override
    public void addResourceHandlerExternalPatternSinkStrategyFactory(
        final ResourceHandlerExternalPatternSinkStrategyFactory factory) {
        ensureLoaded().addResourceHandlerExternalPatternSinkStrategyFactory(factory);
    }

    private RefinedStorageNeoForgeApi ensureLoaded() {
        if (delegate == null) {
            throw new IllegalStateException("NeoForge API not loaded yet");
        }
        return delegate;
    }
}
