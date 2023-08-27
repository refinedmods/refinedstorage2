package com.refinedmods.refinedstorage2.platform.common.menu;

import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.WirelessGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class WirelessGridExtendedMenuProvider extends GridExtendedMenuProvider {
    private final PlayerSlotReference slotReference;

    public WirelessGridExtendedMenuProvider(final Grid grid,
                                            final PlatformRegistry<PlatformStorageChannelType<?>>
                                                storageChannelTypeRegistry,
                                            final PlayerSlotReference slotReference) {
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
        slotReference.writeToBuf(buf);
    }
}
