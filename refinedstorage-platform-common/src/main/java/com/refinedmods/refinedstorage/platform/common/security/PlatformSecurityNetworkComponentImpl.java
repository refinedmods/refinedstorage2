package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.api.network.impl.security.SecurityNetworkComponentImpl;
import com.refinedmods.refinedstorage.api.network.security.Permission;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.platform.api.security.PlatformSecurityNetworkComponent;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class PlatformSecurityNetworkComponentImpl extends SecurityNetworkComponentImpl
    implements PlatformSecurityNetworkComponent {
    public PlatformSecurityNetworkComponentImpl(final SecurityPolicy defaultPolicy) {
        super(defaultPolicy);
    }

    @Override
    public boolean isAllowed(final Permission permission, final ServerPlayer player) {
        final MinecraftServer server = player.getServer();
        if (server == null) {
            return false;
        }
        final GameProfile gameProfile = player.getGameProfile();
        if (server.getPlayerList().isOp(gameProfile)) {
            return true;
        }
        final PlayerSecurityActor actor = new PlayerSecurityActor(gameProfile.getId());
        return super.isAllowed(permission, actor);
    }
}
