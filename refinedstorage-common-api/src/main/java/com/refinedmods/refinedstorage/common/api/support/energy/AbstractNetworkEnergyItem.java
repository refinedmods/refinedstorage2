package com.refinedmods.refinedstorage.common.api.support.energy;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReferenceHandlerItem;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public abstract class AbstractNetworkEnergyItem extends AbstractEnergyItem implements SlotReferenceHandlerItem {
    protected final NetworkItemHelper helper;

    protected AbstractNetworkEnergyItem(final Properties properties,
                                        final EnergyItemHelper energyItemHelper,
                                        final NetworkItemHelper helper) {
        super(properties, energyItemHelper);
        this.helper = helper;
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        return helper.bind(ctx);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return helper.getTooltipImage(stack);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final TooltipDisplay display,
                                final Consumer<Component> builder,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        helper.addTooltip(stack, builder);
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer && level.getServer() != null) {
            final SlotReference slotReference = RefinedStorageApi.INSTANCE.createInventorySlotReference(player, hand);
            slotReference.resolve(player).ifPresent(s -> use(serverPlayer, s, slotReference));
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void use(final ServerPlayer player, final ItemStack stack, final SlotReference slotReference) {
        final NetworkItemContext context = RefinedStorageApi.INSTANCE.getNetworkItemHelper().createContext(
            stack,
            player,
            slotReference
        );
        use(stack.get(DataComponents.CUSTOM_NAME), player, slotReference, context);
    }

    protected abstract void use(@Nullable Component name,
                                ServerPlayer player,
                                SlotReference slotReference,
                                NetworkItemContext context);

    public boolean isBound(final ItemStack stack) {
        return helper.isBound(stack);
    }
}
