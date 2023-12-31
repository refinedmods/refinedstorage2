package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

class WirelessGridExtendedMenuProvider implements ExtendedMenuProvider {
    private final Grid grid;
    private final SlotReference slotReference;

    WirelessGridExtendedMenuProvider(final Grid grid, final SlotReference slotReference) {
        this.grid = grid;
        this.slotReference = slotReference;
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        PlatformApi.INSTANCE.writeGridScreenOpeningData(grid, buf);
        PlatformApi.INSTANCE.writeSlotReference(slotReference, buf);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.WIRELESS_GRID;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessGridContainerMenu(syncId, inventory, grid, slotReference);
    }
}
