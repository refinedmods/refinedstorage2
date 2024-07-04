package com.refinedmods.refinedstorage.platform.common.support.network.bounditem;

import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemHelper;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemSession;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemTargetBlockEntity;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import java.util.List;
import java.util.Optional;

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

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class NetworkBoundItemHelperImpl implements NetworkBoundItemHelper {
    private static final MutableComponent UNBOUND = createTranslation("item", "network_item.unbound")
        .withStyle(ChatFormatting.RED);
    private static final Component UNBOUND_HELP = createTranslation("item", "network_item.unbound.help");

    @Override
    public boolean isBound(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getNetworkLocation());
    }

    @Override
    public void addTooltip(final ItemStack stack, final List<Component> lines) {
        getNetworkLocation(stack).ifPresentOrElse(
            network -> lines.add(createTranslation(
                "item",
                "network_item.bound_to",
                network.pos().getX(),
                network.pos().getY(),
                network.pos().getZ()
            ).withStyle(ChatFormatting.GRAY)),
            () -> lines.add(UNBOUND)
        );
    }

    @Override
    public InteractionResult bind(final UseOnContext ctx) {
        if (ctx.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        final ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
        final BlockEntity blockEntity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (!(blockEntity instanceof NetworkBoundItemTargetBlockEntity)) {
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
    public NetworkBoundItemSession openSession(final ItemStack stack,
                                               final ServerPlayer player,
                                               final SlotReference slotReference) {
        final Optional<GlobalPos> location = getNetworkLocation(stack);
        return new NetworkBoundItemSessionImpl(
            player,
            slotReference,
            location.orElse(null)
        );
    }

    private Optional<GlobalPos> getNetworkLocation(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getNetworkLocation()));
    }
}
