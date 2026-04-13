package com.refinedmods.refinedstorage.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.common.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.ContentNames;

import java.util.Set;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

class SecurityCardExtendedMenuProvider extends AbstractSecurityCardExtendedMenuProvider<PlayerBoundSecurityCardData> {
    @Nullable
    private final Component name;
    private final MinecraftServer server;
    private final SlotReference slotReference;
    private final SecurityCardBoundPlayer boundPlayer;

    SecurityCardExtendedMenuProvider(
        @Nullable final Component name,
        final MinecraftServer server,
        final SlotReference slotReference,
        final SecurityPolicy securityPolicy,
        final Set<PlatformPermission> dirtyPermissions,
        final SecurityCardBoundPlayer boundPlayer
    ) {
        super(securityPolicy, dirtyPermissions);
        this.name = name;
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
        return name == null ? ContentNames.SECURITY_CARD : name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new SecurityCardContainerMenu(syncId, inventory, slotReference);
    }
}
