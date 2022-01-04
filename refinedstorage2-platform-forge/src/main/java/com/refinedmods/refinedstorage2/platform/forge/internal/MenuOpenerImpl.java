package com.refinedmods.refinedstorage2.platform.forge.internal;

import com.refinedmods.refinedstorage2.platform.abstractions.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraftforge.network.NetworkHooks;

public class MenuOpenerImpl implements MenuOpener {
    @Override
    public void openMenu(ServerPlayer player, MenuProvider menuProvider) {
        if (menuProvider instanceof ExtendedMenuProvider extendedMenuProvider) {
            NetworkHooks.openGui(player, menuProvider, buf -> extendedMenuProvider.writeScreenOpeningData(player, buf));
        } else {
            player.openMenu(menuProvider);
        }
    }
}
