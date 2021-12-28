package com.refinedmods.refinedstorage2.platform.abstractions.menu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface MenuOpener {
    void openMenu(ServerPlayer player, MenuProvider menuProvider);
}
