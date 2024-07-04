package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;

import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

class SecurityCardExtendedMenuProvider extends AbstractSecurityCardExtendedMenuProvider<PlayerBoundSecurityCardData> {
    private final MinecraftServer server;
    private final SlotReference slotReference;
    private final SecurityCardBoundPlayer boundPlayer;

    SecurityCardExtendedMenuProvider(final MinecraftServer server,
                                     final SlotReference slotReference,
                                     final SecurityPolicy securityPolicy,
                                     final Set<PlatformPermission> dirtyPermissions,
                                     final SecurityCardBoundPlayer boundPlayer) {
        super(securityPolicy, dirtyPermissions);
        this.server = server;
        this.slotReference = slotReference;
        this.boundPlayer = boundPlayer;
    }

    @Override
    public PlayerBoundSecurityCardData getMenuData() {
        return new PlayerBoundSecurityCardData(
            new SecurityCardData(slotReference, getDataPermissions()),
            PlayerBoundSecurityCardData.Player.of(boundPlayer),
            server.getPlayerList().getPlayers().stream().map(PlayerBoundSecurityCardData.Player::of).toList()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PlayerBoundSecurityCardData> getMenuCodec() {
        return PlayerBoundSecurityCardData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.SECURITY_CARD;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new SecurityCardContainerMenu(syncId, inventory, slotReference);
    }
}
