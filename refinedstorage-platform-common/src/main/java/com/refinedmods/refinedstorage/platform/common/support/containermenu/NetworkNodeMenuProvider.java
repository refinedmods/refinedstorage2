package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import com.refinedmods.refinedstorage.platform.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.common.security.BuiltinPermission;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface NetworkNodeMenuProvider extends MenuProvider {
    Set<InWorldNetworkNodeContainer> getContainers();

    default boolean canOpen(final ServerPlayer player) {
        return SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, getContainers());
    }
}
