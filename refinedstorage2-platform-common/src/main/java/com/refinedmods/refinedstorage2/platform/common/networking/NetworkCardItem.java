package com.refinedmods.refinedstorage2.platform.common.networking;

import com.refinedmods.refinedstorage2.platform.api.support.HelpTooltipComponent;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO: better active texture.
public class NetworkCardItem extends Item {
    private static final MutableComponent UNBOUND_HELP = createTranslation("item", "network_card.unbound_help");
    private static final MutableComponent BOUND_HELP = createTranslation("item", "network_card.bound_help");

    private static final MutableComponent UNBOUND = createTranslation("item", "network_card.unbound")
        .withStyle(ChatFormatting.RED);

    private static final String TAG_POS = "pos";
    private static final String TAG_DIMENSION = "dim";

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
        final CompoundTag tag = new CompoundTag();
        tag.putLong(TAG_POS, pos.asLong());
        tag.putString(TAG_DIMENSION, ctx.getLevel().dimension().location().toString());
        ctx.getItemInHand().setTag(tag);
        ctx.getPlayer().sendSystemMessage(createTranslation(
            "item",
            "network_card.bound",
            pos.getX(),
            pos.getY(),
            pos.getZ()
        ));
        return InteractionResult.SUCCESS;
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
                                @Nullable final Level level,
                                final List<Component> lines,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (!isActive(stack)) {
            lines.add(UNBOUND);
            return;
        }
        final BlockPos pos = getPosition(stack);
        if (pos == null) {
            return;
        }
        lines.add(createTranslation(
            "item",
            "network_card.bound",
            pos.getX(),
            pos.getY(),
            pos.getZ()
        ).withStyle(ChatFormatting.GRAY));
    }

    @Nullable
    public BlockPos getPosition(final ItemStack stack) {
        if (stack.getTag() == null) {
            return null;
        }
        return BlockPos.of(stack.getTag().getLong(TAG_POS));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(isActive(stack) ? BOUND_HELP : UNBOUND_HELP));
    }

    boolean isActive(final ItemStack stack) {
        return stack.getTag() != null && stack.getTag().contains(TAG_POS) && stack.getTag().contains(TAG_DIMENSION);
    }
}
