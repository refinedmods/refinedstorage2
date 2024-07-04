package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.platform.common.content.DataComponents;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.platform.common.networking.NetworkReceiverKey.getDimensionName;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class NetworkCardItem extends Item {
    private static final MutableComponent UNBOUND_HELP = createTranslation("item", "network_card.unbound_help");
    private static final MutableComponent BOUND_HELP = createTranslation("item", "network_card.bound_help");
    private static final MutableComponent UNBOUND = createTranslation("item", "network_card.unbound")
        .withStyle(ChatFormatting.RED);

    public NetworkCardItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        if (ctx.getLevel().isClientSide() || ctx.getPlayer() == null) {
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
        ctx.getPlayer().sendSystemMessage(createTranslation(
            "item",
            "network_card.bound",
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            getDimensionName(dimension).withStyle(ChatFormatting.YELLOW)
        ));
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
        if (player.isCrouching()) {
            if (!level.isClientSide()) {
                player.sendSystemMessage(createTranslation("item", "network_card.unbound"));
            }
            return new InteractionResultHolder<>(InteractionResult.CONSUME, new ItemStack(this));
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, lines, flag);
        getLocation(stack).ifPresentOrElse(location -> lines.add(createTranslation(
            "item",
            "network_card.bound",
            location.pos().getX(),
            location.pos().getY(),
            location.pos().getZ(),
            getDimensionName(location.dimension()).withStyle(ChatFormatting.YELLOW)
        ).withStyle(ChatFormatting.GRAY)), () -> lines.add(UNBOUND));
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
