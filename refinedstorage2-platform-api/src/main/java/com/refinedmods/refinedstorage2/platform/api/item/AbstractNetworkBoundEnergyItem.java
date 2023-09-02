package com.refinedmods.refinedstorage2.platform.api.item;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public abstract class AbstractNetworkBoundEnergyItem extends AbstractEnergyItem {
    protected final NetworkBoundItemHelper networkBoundItemHelper;

    protected AbstractNetworkBoundEnergyItem(final Properties properties,
                                             final EnergyItemHelper energyItemHelper,
                                             final NetworkBoundItemHelper networkBoundItemHelper) {
        super(properties, energyItemHelper);
        this.networkBoundItemHelper = networkBoundItemHelper;
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        return networkBoundItemHelper.bind(ctx);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return networkBoundItemHelper.getTooltipImage(stack);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        networkBoundItemHelper.addTooltip(stack, tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        final ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final SlotReference slotReference = PlatformApi.INSTANCE.createInventorySlotReference(player, hand);
            final NetworkBoundItemSession session = networkBoundItemHelper.openSession(
                stack,
                serverPlayer,
                slotReference
            );
            use(serverPlayer, slotReference, session);
        }
        return InteractionResultHolder.consume(stack);
    }

    public abstract void use(ServerPlayer player, SlotReference slotReference, NetworkBoundItemSession session);

    public boolean isBound(final ItemStack stack) {
        return networkBoundItemHelper.isBound(stack);
    }
}
