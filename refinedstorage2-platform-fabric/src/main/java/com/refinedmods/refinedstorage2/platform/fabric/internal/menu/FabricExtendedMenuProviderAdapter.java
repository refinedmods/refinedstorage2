package com.refinedmods.refinedstorage2.platform.fabric.internal.menu;

import com.refinedmods.refinedstorage2.platform.abstractions.menu.ExtendedMenuProvider;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class FabricExtendedMenuProviderAdapter implements ExtendedScreenHandlerFactory {
    private final ExtendedMenuProvider extendedMenuProvider;

    public FabricExtendedMenuProviderAdapter(ExtendedMenuProvider extendedMenuProvider) {
        this.extendedMenuProvider = extendedMenuProvider;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        extendedMenuProvider.writeScreenOpeningData(player, buf);
    }

    @Override
    public Component getDisplayName() {
        return extendedMenuProvider.getDisplayName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player player) {
        return extendedMenuProvider.createMenu(syncId, inventory, player);
    }
}
