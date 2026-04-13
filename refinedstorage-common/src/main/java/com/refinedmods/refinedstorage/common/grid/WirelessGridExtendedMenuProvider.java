package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

class WirelessGridExtendedMenuProvider implements ExtendedMenuProvider<WirelessGridData> {
    private final Component name;
    private final Grid grid;
    private final SlotReference slotReference;

    WirelessGridExtendedMenuProvider(final Component name, final Grid grid, final SlotReference slotReference) {
        this.name = name;
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
        return name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessGridContainerMenu(syncId, inventory, grid, slotReference);
    }
}
