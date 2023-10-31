package com.refinedmods.refinedstorage2.platform.fabric.support.containermenu;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class FabricExtendedMenuProviderAdapter implements ExtendedScreenHandlerFactory {
    private final ExtendedMenuProvider extendedMenuProvider;

    public FabricExtendedMenuProviderAdapter(final ExtendedMenuProvider extendedMenuProvider) {
        this.extendedMenuProvider = extendedMenuProvider;
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        extendedMenuProvider.writeScreenOpeningData(player, buf);
    }

    @Override
    public Component getDisplayName() {
        return extendedMenuProvider.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return extendedMenuProvider.createMenu(syncId, inventory, player);
    }
}
