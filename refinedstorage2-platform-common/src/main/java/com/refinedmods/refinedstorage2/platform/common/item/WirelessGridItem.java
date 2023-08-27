package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.WirelessGrid;
import com.refinedmods.refinedstorage2.platform.common.menu.WirelessGridExtendedMenuProvider;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WirelessGridItem extends AbstractNetworkBoundItem {
    public WirelessGridItem(final ItemEnergyProvider energyProvider) {
        super(new Item.Properties().stacksTo(1), energyProvider);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final PlayerSlotReference slotReference = PlayerSlotReference.of(player, hand);
            tryUse(
                stack,
                serverPlayer,
                slotReference,
                ctx -> open(level.getServer(), serverPlayer, ctx, slotReference)
            );
        }
        return InteractionResultHolder.consume(stack);
    }

    private void open(
        final MinecraftServer server,
        final ServerPlayer player,
        final NetworkBoundItemContext ctx,
        final PlayerSlotReference slotReference
    ) {
        ctx.drain(Platform.INSTANCE.getConfig().getWirelessGrid().getOpenEnergyUsage());
        final Grid grid = new WirelessGrid(server, ctx);
        Platform.INSTANCE.getMenuOpener().openMenu(player, new WirelessGridExtendedMenuProvider(
            grid,
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry(),
            slotReference
        ));
    }
}
