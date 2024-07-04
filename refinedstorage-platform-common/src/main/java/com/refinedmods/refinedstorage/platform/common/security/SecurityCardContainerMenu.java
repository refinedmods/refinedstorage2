package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.content.Menus;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.platform.common.support.stretching.ScreenSizeListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SecurityCardContainerMenu extends AbstractSecurityCardContainerMenu implements ScreenSizeListener {
    private final List<PlayerBoundSecurityCardData.Player> players;
    private PlayerBoundSecurityCardData.Player boundTo;

    public SecurityCardContainerMenu(final int syncId,
                                     final Inventory playerInventory,
                                     final PlayerBoundSecurityCardData playerBoundSecurityCardData) {
        super(
            Menus.INSTANCE.getSecurityCard(),
            syncId,
            playerInventory,
            playerBoundSecurityCardData.securityCardData()
        );
        this.boundTo = playerBoundSecurityCardData.boundTo();
        this.players = playerBoundSecurityCardData.players();
    }

    SecurityCardContainerMenu(final int syncId, final Inventory playerInventory, final SlotReference disabledSlot) {
        super(Menus.INSTANCE.getSecurityCard(), syncId, playerInventory, disabledSlot);
        this.boundTo = new PlayerBoundSecurityCardData.Player(UUID.randomUUID(), "");
        this.players = new ArrayList<>();
    }

    List<PlayerBoundSecurityCardData.Player> getPlayers() {
        return players;
    }

    PlayerBoundSecurityCardData.Player getBoundTo() {
        return boundTo;
    }

    public void setBoundPlayer(final MinecraftServer server, final UUID playerId) {
        if (disabledSlot == null) {
            return;
        }
        disabledSlot.resolve(playerInventory.player).ifPresent(stack -> setBoundPlayer(server, playerId, stack));
    }

    private void setBoundPlayer(final MinecraftServer server, final UUID playerId, final ItemStack stack) {
        if (stack.getItem() instanceof SecurityCardItem securityCardItem) {
            final ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player == null) {
                return;
            }
            securityCardItem.setBoundPlayer(player, stack);
        }
    }

    void changeBoundPlayer(final PlayerBoundSecurityCardData.Player player) {
        C2SPackets.sendSecurityCardBoundPlayer(player.id());
        this.boundTo = player;
    }
}
