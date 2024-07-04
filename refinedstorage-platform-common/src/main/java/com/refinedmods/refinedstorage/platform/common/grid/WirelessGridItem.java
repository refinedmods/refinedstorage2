package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.platform.api.support.energy.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.security.BuiltinPermission;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WirelessGridItem extends AbstractNetworkBoundEnergyItem {
    public WirelessGridItem() {
        super(
            new Item.Properties().stacksTo(1),
            PlatformApi.INSTANCE.getEnergyItemHelper(),
            PlatformApi.INSTANCE.getNetworkBoundItemHelper()
        );
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getWirelessGrid().getEnergyCapacity()
        );
        return PlatformApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }

    @Override
    public void use(final ServerPlayer player,
                    final SlotReference slotReference,
                    final NetworkBoundItemSession session) {
        final boolean isAllowed = session.resolveNetwork()
            .map(network -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network))
            .orElse(true); // if the network can't be resolved that will be apparent later in the UI.
        if (!isAllowed) {
            PlatformApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.WIRELESS_GRID);
            return;
        }
        final Grid grid = new WirelessGrid(session);
        Platform.INSTANCE.getMenuOpener().openMenu(player, new WirelessGridExtendedMenuProvider(grid, slotReference));
    }
}
