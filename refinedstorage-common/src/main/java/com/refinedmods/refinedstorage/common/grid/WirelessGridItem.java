package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.energy.EnergyItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.common.support.energy.ItemEnergyStorage;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNullElse;

public class WirelessGridItem extends AbstractNetworkEnergyItem {
    private final boolean creative;

    public WirelessGridItem(final boolean creative) {
        super(
            new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM,
                creative ? ContentIds.CREATIVE_WIRELESS_GRID : ContentIds.WIRELESS_GRID)),
            RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
            RefinedStorageApi.INSTANCE.getNetworkItemHelper()
        );
        this.creative = creative;
    }

    public static EnergyStorage createEnergyStorage(final ItemStack stack, final EnergyItemContext context) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Math.clamp(Platform.INSTANCE.getConfig().getWirelessGrid().getEnergyCapacity(), 1, Long.MAX_VALUE)
        );
        return new ItemEnergyStorage(stack, energyStorage, context);
    }

    @Override
    protected void use(@Nullable final Component name,
                       final ServerPlayer player,
                       final PlayerSlotReference playerSlotReference,
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
        final var provider = new WirelessGridExtendedMenuProvider(correctedName, grid, playerSlotReference);
        Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }
}
