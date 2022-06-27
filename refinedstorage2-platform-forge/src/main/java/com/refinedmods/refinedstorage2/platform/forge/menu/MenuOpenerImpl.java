package com.refinedmods.refinedstorage2.platform.forge.menu;

import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.menu.MenuOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;

public class MenuOpenerImpl implements MenuOpener {
    @Override
    public void openMenu(final ServerPlayer player, final MenuProvider menuProvider) {
        if (menuProvider instanceof ExtendedMenuProvider extendedMenuProvider) {
            NetworkHooks.openGui(player, menuProvider, buf -> extendedMenuProvider.writeScreenOpeningData(player, buf));
        } else {
            player.openMenu(menuProvider);
        }
    }
}
