package com.refinedmods.refinedstorage.platform.neoforge.support.containermenu;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.MenuOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public class MenuOpenerImpl implements MenuOpener {
    @Override
    public void openMenu(final ServerPlayer player, final MenuProvider menuProvider) {
        if (menuProvider instanceof ExtendedMenuProvider<?> extendedMenuProvider) {
            openExtendedMenu(player, extendedMenuProvider);
        } else {
            player.openMenu(menuProvider);
        }
    }

    private static <T> void openExtendedMenu(final ServerPlayer player,
                                             final ExtendedMenuProvider<T> extendedMenuProvider) {
        player.openMenu(
            extendedMenuProvider,
            buf -> extendedMenuProvider.getMenuCodec().encode(buf, extendedMenuProvider.getMenuData())
        );
    }
}
