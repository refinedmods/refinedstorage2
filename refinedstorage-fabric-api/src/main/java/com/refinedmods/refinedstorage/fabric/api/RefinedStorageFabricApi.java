package com.refinedmods.refinedstorage.fabric.api;

import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.5")
public interface RefinedStorageFabricApi {
    RefinedStorageFabricApi INSTANCE = new RefinedStorageFabricApiProxy();

    BlockApiLookup<NetworkNodeContainerProvider, @Nullable Direction> getNetworkNodeContainerProviderLookup();

    void addStorageExternalPatternSinkStrategyFactory(StorageExternalPatternSinkStrategyFactory factory);
}
