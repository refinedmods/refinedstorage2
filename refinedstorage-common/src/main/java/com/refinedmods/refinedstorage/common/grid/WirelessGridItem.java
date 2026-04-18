package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static java.util.Objects.requireNonNullElse;

public class WirelessGridItem extends AbstractNetworkEnergyItem {
    private final boolean creative;

    public WirelessGridItem(final boolean creative) {
        super(
            new Item.Properties().stacksTo(1),
            RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
            RefinedStorageApi.INSTANCE.getNetworkItemHelper()
        );
        this.creative = creative;
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Math.clamp(Platform.INSTANCE.getConfig().getWirelessGrid().getEnergyCapacity(), 1, Long.MAX_VALUE)
        );
        return RefinedStorageApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }

    @Override
    protected void use(@Nullable final Component name,
                       final ServerPlayer player,
                       final SlotReference slotReference,
                       final NetworkItemContext context) {
        final boolean isAllowed = context.resolveNetwork()
            .map(network -> SecurityHelper.isAllowed(player, BuiltinPermission.OPEN, network))
            .orElse(true);
        if (!isAllowed) {
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(player, ContentNames.WIRELESS_GRID);
            return;
        }
        final Grid grid = new WirelessGrid(context);
        final Component correctedName = requireNonNullElse(
            name,
            creative ? ContentNames.CREATIVE_WIRELESS_GRID : ContentNames.WIRELESS_GRID
        );
        final var provider = new WirelessGridExtendedMenuProvider(correctedName, grid, slotReference);
        Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }
}
