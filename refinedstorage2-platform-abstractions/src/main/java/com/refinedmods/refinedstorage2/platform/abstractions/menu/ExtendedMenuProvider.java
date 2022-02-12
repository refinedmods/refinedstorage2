package com.refinedmods.refinedstorage2.platform.abstractions.menu;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;

public interface ExtendedMenuProvider extends MenuProvider {
    void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf);
}
