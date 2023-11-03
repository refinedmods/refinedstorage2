package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.support.energy.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WirelessGridItem extends AbstractNetworkBoundEnergyItem {
    private final boolean creative;

    public WirelessGridItem(final boolean creative) {
        super(
            new Item.Properties().stacksTo(1),
            PlatformApi.INSTANCE.getEnergyItemHelper(),
            PlatformApi.INSTANCE.getNetworkBoundItemHelper()
        );
        this.creative = creative;
    }

    @Override
    public Optional<EnergyStorage> createEnergyStorage(final ItemStack stack) {
        if (creative) {
            return Optional.empty();
        }
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getWirelessGrid().getEnergyCapacity()
        );
        return Optional.of(PlatformApi.INSTANCE.asItemEnergyStorage(energyStorage, stack));
    }

    @Override
    public void use(final ServerPlayer player,
                    final SlotReference slotReference,
                    final NetworkBoundItemSession session) {
        session.drainEnergy(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        final Grid grid = new WirelessGrid(session);
        Platform.INSTANCE.getMenuOpener().openMenu(player, new WirelessGridExtendedMenuProvider(
            grid,
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry(),
            slotReference
        ));
    }
}
