package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.item.AbstractEnergyItem;
import com.refinedmods.refinedstorage2.platform.api.item.HelpTooltipComponent;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class AbstractNetworkBoundItem extends AbstractEnergyItem {
    private static final MutableComponent UNBOUND = createTranslation("item", "network_item.unbound")
        .withStyle(ChatFormatting.RED);
    private static final Component UNBOUND_HELP = createTranslation("item", "network_item.unbound.help");

    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_DIMENSION = "dim";

    protected AbstractNetworkBoundItem(final Properties properties) {
        super(properties, PlatformApi.INSTANCE.getEnergyItemHelper());
    }

    public boolean isBound(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        return tag.contains(TAG_X) && tag.contains(TAG_Y) && tag.contains(TAG_Z) && tag.contains(TAG_DIMENSION);
    }

    private Optional<NetworkBoundItemContext.NetworkReference> getNetworkReference(final ItemStack stack) {
        if (!isBound(stack)) {
            return Optional.empty();
        }
        final CompoundTag tag = Objects.requireNonNull(stack.getTag());
        final int x = tag.getInt(TAG_X);
        final int y = tag.getInt(TAG_Y);
        final int z = tag.getInt(TAG_Z);
        final ResourceLocation dimension = new ResourceLocation(tag.getString(TAG_DIMENSION));
        return Optional.of(new NetworkBoundItemContext.NetworkReference(
            ResourceKey.create(Registries.DIMENSION, dimension),
            new BlockPos(x, y, z)
        ));
    }

    protected void tryUse(
        final ItemStack stack,
        final ServerPlayer player,
        final PlayerSlotReference slotReference,
        final Consumer<NetworkBoundItemContext> contextConsumer
    ) {
        final Optional<NetworkBoundItemContext.NetworkReference> networkReference = getNetworkReference(stack);
        contextConsumer.accept(new NetworkBoundItemContext(
            player,
            slotReference,
            networkReference.orElse(null)
        ));
    }

    @Override
    public InteractionResult useOn(final UseOnContext ctx) {
        if (ctx.getPlayer() == null) {
            return InteractionResult.PASS;
        }
        final ItemStack stack = ctx.getPlayer().getItemInHand(ctx.getHand());
        final BlockEntity blockEntity = ctx.getLevel().getBlockEntity(ctx.getClickedPos());
        if (!(blockEntity instanceof PlatformNetworkNodeContainer)) {
            return InteractionResult.PASS;
        }
        final CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(TAG_X, blockEntity.getBlockPos().getX());
        tag.putInt(TAG_Y, blockEntity.getBlockPos().getY());
        tag.putInt(TAG_Z, blockEntity.getBlockPos().getZ());
        tag.putString(TAG_DIMENSION, ctx.getLevel().dimension().location().toString());
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(final ItemStack stack,
                                @Nullable final Level level,
                                final List<Component> tooltip,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        getNetworkReference(stack).ifPresentOrElse(
            network -> tooltip.add(createTranslation(
                "item",
                "network_item.bound_to",
                network.pos().getX(),
                network.pos().getY(),
                network.pos().getZ()
            ).withStyle(ChatFormatting.GRAY)),
            () -> tooltip.add(UNBOUND)
        );
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        if (isBound(stack)) {
            return Optional.empty();
        }
        return Optional.of(new HelpTooltipComponent(UNBOUND_HELP));
    }
}
