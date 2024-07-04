package com.refinedmods.refinedstorage.platform.common.content;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

@FunctionalInterface
public interface ExtendedMenuTypeFactory {
    <T extends AbstractContainerMenu, D> MenuType<T> create(
        MenuSupplier<T, D> supplier,
        StreamCodec<RegistryFriendlyByteBuf, D> streamCodec
    );

    @FunctionalInterface
    interface MenuSupplier<T extends AbstractContainerMenu, D> {
        T create(int syncId, Inventory playerInventory, D data);
    }
}
