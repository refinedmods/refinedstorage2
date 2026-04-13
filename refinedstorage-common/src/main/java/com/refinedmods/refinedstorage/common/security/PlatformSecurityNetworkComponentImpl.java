package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.impl.security.SecurityNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.common.api.security.PlatformSecurityNetworkComponent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

public class PlatformSecurityNetworkComponentImpl extends SecurityNetworkComponentImpl
    implements PlatformSecurityNetworkComponent {
    public PlatformSecurityNetworkComponentImpl(final SecurityPolicy defaultPolicy) {
        super(defaultPolicy);
    }

    @Override
    public boolean isAllowed(final Permission permission, final ServerPlayer player) {
        final ServerLevel level = player.level();
        final MinecraftServer server = level.getServer();
        final NameAndId nameAndId = player.nameAndId();
        if (server.getPlayerList().isOp(nameAndId)) {
            return true;
        }
        final PlayerSecurityActor actor = new PlayerSecurityActor(nameAndId.id());
        return super.isAllowed(permission, actor);
    }
}
