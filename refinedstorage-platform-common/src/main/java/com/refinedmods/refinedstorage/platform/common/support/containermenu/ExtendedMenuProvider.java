package com.refinedmods.refinedstorage.platform.common.support.containermenu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.MenuProvider;

public interface ExtendedMenuProvider<T> extends MenuProvider {
    T getMenuData();

    StreamEncoder<RegistryFriendlyByteBuf, T> getMenuCodec();
}
