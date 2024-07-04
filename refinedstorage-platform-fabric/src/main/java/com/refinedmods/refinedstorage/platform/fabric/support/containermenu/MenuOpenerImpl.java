package com.refinedmods.refinedstorage.platform.fabric.support.containermenu;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.MenuOpener;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class MenuOpenerImpl implements MenuOpener {
    @Override
    public void openMenu(final ServerPlayer player, final MenuProvider menuProvider) {
        if (menuProvider instanceof ExtendedMenuProvider<?> extendedMenuProvider) {
            openExtendedMenu(player, extendedMenuProvider);
        } else {
            player.openMenu(menuProvider);
        }
    }

    private <T> void openExtendedMenu(final ServerPlayer player, final ExtendedMenuProvider<T> extendedMenuProvider) {
        player.openMenu(new ExtendedScreenHandlerFactory<T>() {
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
                return extendedMenuProvider.createMenu(syncId, inventory, player);
            }

            @Override
            public Component getDisplayName() {
                return extendedMenuProvider.getDisplayName();
            }

            @Override
            public T getScreenOpeningData(final ServerPlayer player) {
                return extendedMenuProvider.getMenuData();
            }
        });
    }
}
