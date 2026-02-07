package com.refinedmods.refinedstorage.common.api.support.network;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.6")
public interface NetworkNodeContainerProvider {
    Set<InWorldNetworkNodeContainer> getContainers();

    void addContainer(InWorldNetworkNodeContainer container);

    boolean canBuild(ServerPlayer player);

    default void update(@Nullable final Level level) {
        getContainers().forEach(container -> RefinedStorageApi.INSTANCE.updateNetworkNodeContainer(container, level));
    }

    default void initialize(@Nullable final Level level, @Nullable final Runnable callback) {
        getContainers().forEach(container -> RefinedStorageApi.INSTANCE.initializeNetworkNodeContainer(
            container,
            level,
            callback
        ));
    }

    default void remove(@Nullable final Level level) {
        getContainers().forEach(container -> RefinedStorageApi.INSTANCE.removeNetworkNodeContainer(container, level));
    }
}
