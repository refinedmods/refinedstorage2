package com.refinedmods.refinedstorage.platform.api.security;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;

import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

public final class SecurityHelper {
    private SecurityHelper() {
    }

    public static boolean isAllowed(final ServerPlayer player,
                                    final Permission permission,
                                    final Set<InWorldNetworkNodeContainer> containers) {
        for (final InWorldNetworkNodeContainer container : containers) {
            if (!isAllowed(player, permission, container.getNode())) {
                return false;
            }
        }
        return true;
    }

    public static boolean isAllowed(final ServerPlayer player, final Permission permission, final NetworkNode node) {
        final Network network = node.getNetwork();
        if (network == null) {
            return false;
        }
        return isAllowed(player, permission, network);
    }

    public static boolean isAllowed(final ServerPlayer player, final Permission permission, final Network network) {
        return network.getComponent(PlatformSecurityNetworkComponent.class).isAllowed(permission, player);
    }
}
