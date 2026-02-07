package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.common.support.containermenu.ExtendedMenuProvider;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jspecify.annotations.Nullable;

class WirelessAutocraftingMonitorExtendedMenuProvider implements ExtendedMenuProvider<AutocraftingMonitorData> {
    private final Component name;
    private final AutocraftingMonitor autocraftingMonitor;

    WirelessAutocraftingMonitorExtendedMenuProvider(final Component name,
                                                    final AutocraftingMonitor autocraftingMonitor) {
        this.name = name;
        this.autocraftingMonitor = autocraftingMonitor;
    }

    @Override
    public AutocraftingMonitorData getMenuData() {
        return new AutocraftingMonitorData(
            autocraftingMonitor.getStatuses(),
            autocraftingMonitor.isAutocraftingMonitorActive()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, AutocraftingMonitorData> getMenuCodec() {
        return AutocraftingMonitorData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return name;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new WirelessAutocraftingMonitorContainerMenu(syncId, inventory, autocraftingMonitor);
    }
}
