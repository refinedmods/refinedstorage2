package com.refinedmods.refinedstorage2.core;

import com.refinedmods.refinedstorage2.core.network.NetworkManager;
import net.minecraft.server.world.ServerWorld;

public interface RefinedStorage2ApiFacade {
    NetworkManager getNetworkManager(ServerWorld world);
}
