package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ExtendedMenuProvider;

import javax.annotation.Nullable;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

class WirelessGridExtendedMenuProvider implements ExtendedMenuProvider<WirelessGridData> {
    private final Grid grid;
    private final SlotReference slotReference;

    WirelessGridExtendedMenuProvider(final Grid grid, final SlotReference slotReference) {
        this.grid = grid;
        this.slotReference = slotReference;
    }

    @Override
    public WirelessGridData getMenuData() {
        return new WirelessGridData(
            GridData.of(grid),
            slotReference
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, WirelessGridData> getMenuCodec() {
        return WirelessGridData.STREAM_CODEC;
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
