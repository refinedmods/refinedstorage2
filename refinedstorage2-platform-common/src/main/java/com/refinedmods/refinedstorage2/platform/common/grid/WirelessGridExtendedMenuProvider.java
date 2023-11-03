package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

class WirelessGridExtendedMenuProvider extends GridExtendedMenuProvider {
    private final SlotReference slotReference;

    WirelessGridExtendedMenuProvider(final Grid grid,
                                     final PlatformRegistry<PlatformStorageChannelType<?>>
                                         storageChannelTypeRegistry,
                                     final SlotReference slotReference) {
        super(grid, storageChannelTypeRegistry, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return ContentNames.WIRELESS_GRID;
            }

            @Override
            public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
                return new WirelessGridContainerMenu(syncId, inventory, grid, slotReference);
            }
        });
        this.slotReference = slotReference;
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        super.writeScreenOpeningData(player, buf);
        PlatformApi.INSTANCE.writeSlotReference(slotReference, buf);
    }
}
