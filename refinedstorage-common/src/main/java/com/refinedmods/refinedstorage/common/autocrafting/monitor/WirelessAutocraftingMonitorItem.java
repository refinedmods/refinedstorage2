package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.security.SecurityHelper;
import com.refinedmods.refinedstorage.common.api.support.energy.AbstractNetworkEnergyItem;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.security.BuiltinPermission;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNullElse;

public class WirelessAutocraftingMonitorItem extends AbstractNetworkEnergyItem {
    private final boolean creative;

    public WirelessAutocraftingMonitorItem(final boolean creative) {
        super(
            new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, creative
                ? ContentIds.CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR : ContentIds.WIRELESS_AUTOCRAFTING_MONITOR)),
            RefinedStorageApi.INSTANCE.getEnergyItemHelper(),
            RefinedStorageApi.INSTANCE.getNetworkItemHelper()
        );
        this.creative = creative;
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getWirelessAutocraftingMonitor().getEnergyCapacity()
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
            RefinedStorageApi.INSTANCE.sendNoPermissionToOpenMessage(
                player,
                ContentNames.WIRELESS_AUTOCRAFTING_MONITOR
            );
            return;
        }
        final WirelessAutocraftingMonitor autocraftingMonitor = new WirelessAutocraftingMonitor(context);
        final Component correctedName = requireNonNullElse(
            name,
            creative ? ContentNames.CREATIVE_WIRELESS_AUTOCRAFTING_MONITOR : ContentNames.WIRELESS_AUTOCRAFTING_MONITOR
        );
        final var provider = new WirelessAutocraftingMonitorExtendedMenuProvider(correctedName, autocraftingMonitor);
        Platform.INSTANCE.getMenuOpener().openMenu(player, provider);
    }
}
