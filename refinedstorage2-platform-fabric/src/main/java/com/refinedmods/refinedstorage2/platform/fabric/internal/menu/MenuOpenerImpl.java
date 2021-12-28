package com.refinedmods.refinedstorage2.platform.fabric.internal.menu;

import com.refinedmods.refinedstorage2.platform.abstractions.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.MenuOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public class MenuOpenerImpl implements MenuOpener {
    @Override
    public void openMenu(ServerPlayer player, MenuProvider menuProvider) {
        if (menuProvider instanceof ExtendedMenuProvider extendedMenuProvider) {
            menuProvider = new FabricExtendedMenuProviderAdapter(extendedMenuProvider);
        }
        player.openMenu(menuProvider);
    }
}
