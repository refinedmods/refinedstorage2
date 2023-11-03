package com.refinedmods.refinedstorage2.platform.common.content;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

@FunctionalInterface
public interface MenuTypeFactory {
    <T extends AbstractContainerMenu> MenuType<T> create(MenuSupplier<T> supplier);

    @FunctionalInterface
    interface MenuSupplier<T extends AbstractContainerMenu> {
        T create(int syncId, Inventory playerInventory, FriendlyByteBuf data);
    }
}
