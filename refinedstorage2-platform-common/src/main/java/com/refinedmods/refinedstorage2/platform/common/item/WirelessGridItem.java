package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractNetworkBoundEnergyItem;
import com.refinedmods.refinedstorage2.platform.api.item.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.WirelessGrid;
import com.refinedmods.refinedstorage2.platform.common.menu.WirelessGridExtendedMenuProvider;

import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

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
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final NetworkBoundItemSession session = networkBoundItemHelper.openSession(stack, serverPlayer, hand);
            open(serverPlayer, hand, session);
        }
        return InteractionResultHolder.consume(stack);
    }

    private void open(final ServerPlayer player, final InteractionHand hand, final NetworkBoundItemSession session) {
        session.drainEnergy(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        final Grid grid = new WirelessGrid(session);
        Platform.INSTANCE.getMenuOpener().openMenu(player, new WirelessGridExtendedMenuProvider(
            grid,
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry(),
            PlayerSlotReference.of(player, hand)
        ));
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
}
