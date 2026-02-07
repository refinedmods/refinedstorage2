package com.refinedmods.refinedstorage.common.support.network.item;

import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemContext;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemHelper;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.slotreference.SlotReference;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NetworkItemHelperImpl implements NetworkItemHelper {
    private static final MutableComponent UNBOUND = createTranslation("item", "network_item.unbound")
        .withStyle(ChatFormatting.RED);
    private static final Component UNBOUND_HELP = createTranslation("item", "network_item.unbound.help");

    @Override
    public boolean isBound(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getNetworkLocation());
    }

    @Override
    public void addTooltip(final ItemStack stack, final Consumer<Component> builder) {
        getNetworkLocation(stack).ifPresentOrElse(network -> builder.accept(createTranslation(
            "item",
            "network_item.bound_to",
            network.pos().getX(),
            network.pos().getY(),
            network.pos().getZ()
        ).withStyle(ChatFormatting.GRAY)), () -> builder.accept(UNBOUND));
    }

    @Override
    public InteractionResult bind(final UseOnContext ctx) {
        if (ctx.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        final ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
        final BlockEntity blockEntity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (!(blockEntity instanceof NetworkItemTargetBlockEntity)) {
            return InteractionResult.PASS;
        }
        final GlobalPos pos = GlobalPos.of(ctx.getLevel().dimension(), blockEntity.getBlockPos());
        stack.set(DataComponents.INSTANCE.getNetworkLocation(), pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        if (isBound(stack)) {
            return Optional.empty();
        }
        return Optional.of(new HelpTooltipComponent(UNBOUND_HELP));
    }

    @Override
    public NetworkItemContext createContext(final ItemStack stack,
                                            final ServerPlayer player,
                                            final SlotReference slotReference) {
        final Optional<GlobalPos> location = getNetworkLocation(stack);
        return new NetworkItemContextImpl(
            player,
            slotReference,
            location.orElse(null)
        );
    }

    private Optional<GlobalPos> getNetworkLocation(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getNetworkLocation()));
    }
}
