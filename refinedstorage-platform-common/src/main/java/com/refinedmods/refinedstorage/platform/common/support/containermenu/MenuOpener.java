package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface MenuOpener {
    void openMenu(ServerPlayer player, MenuProvider menuProvider);
}
