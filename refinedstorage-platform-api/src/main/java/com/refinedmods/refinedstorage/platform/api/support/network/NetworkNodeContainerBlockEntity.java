package com.refinedmods.refinedstorage.platform.api.support.network;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.SecurityHelper;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
@FunctionalInterface
public interface NetworkNodeContainerBlockEntity {
    Set<InWorldNetworkNodeContainer> getContainers();

    default boolean canBuild(final ServerPlayer player) {
        return SecurityHelper.isAllowed(player, PlatformApi.INSTANCE.getBuiltinPermissions().build(), getContainers());
    }
}
