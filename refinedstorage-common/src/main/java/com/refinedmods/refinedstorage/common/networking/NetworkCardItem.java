package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.ContentIds;
import com.refinedmods.refinedstorage.common.content.DataComponents;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NetworkCardItem extends Item {
    private static final MutableComponent UNBOUND_HELP = createTranslation("item", "network_card.unbound_help");
    private static final MutableComponent BOUND_HELP = createTranslation("item", "network_card.bound_help");
    private static final MutableComponent UNBOUND = createTranslation("item", "network_card.unbound")
        .withStyle(ChatFormatting.RED);

    public NetworkCardItem() {
        super(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, ContentIds.NETWORK_CARD)));
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        if (!(ctx.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }
        final BlockPos pos = ctx.getClickedPos();
        final BlockState blockState = ctx.getLevel().getBlockState(pos);
        if (!(blockState.getBlock() instanceof NetworkReceiverBlock)) {
            return InteractionResult.CONSUME;
        }
        final ResourceKey<Level> dimension = ctx.getLevel().dimension();
        final GlobalPos location = GlobalPos.of(dimension, pos);
        ctx.getItemInHand().set(DataComponents.INSTANCE.getNetworkLocation(), location);
        serverPlayer.sendSystemMessage(createTranslation(
            "item",
            "network_card.bound",
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            NetworkReceiverKey.getDimensionName(dimension).withStyle(ChatFormatting.YELLOW)
        ));
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult use(final Level level, final Player player, final InteractionHand hand) {
        if (player.isCrouching()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(createTranslation("item", "network_card.unbound"));
            }
            return InteractionResult.CONSUME.heldItemTransformedTo(getDefaultInstance());
        }
        return super.use(level, player, hand);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack, final TooltipContext context, final TooltipDisplay display,
                                final Consumer<Component> builder, final TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, builder, tooltipFlag);
        getLocation(stack).ifPresentOrElse(location -> builder.accept(createTranslation(
            "item",
            "network_card.bound",
            location.pos().getX(),
            location.pos().getY(),
            location.pos().getZ(),
            NetworkReceiverKey.getDimensionName(location.dimension()).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.GRAY)), () -> builder.accept(UNBOUND));
    }

    Optional<GlobalPos> getLocation(final ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.INSTANCE.getNetworkLocation()));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(isActive(stack) ? BOUND_HELP : UNBOUND_HELP));
    }

    boolean isActive(final ItemStack stack) {
        return stack.has(DataComponents.INSTANCE.getNetworkLocation());
    }
}
