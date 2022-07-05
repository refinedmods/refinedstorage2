package com.refinedmods.refinedstorage2.platform.fabric.menu;

import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.menu.MenuOpener;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public class MenuOpenerImpl implements MenuOpener {
    @Override
    public void openMenu(final ServerPlayer player, final MenuProvider menuProvider) {
        if (menuProvider instanceof ExtendedMenuProvider extendedMenuProvider) {
            player.openMenu(new FabricExtendedMenuProviderAdapter(extendedMenuProvider));
        } else {
            player.openMenu(menuProvider);
        }
    }
}
