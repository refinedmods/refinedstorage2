package com.refinedmods.refinedstorage2.platform.common.internal.item;

import com.refinedmods.refinedstorage2.platform.api.item.HelpTooltipComponent;
import com.refinedmods.refinedstorage2.platform.api.item.NetworkBoundItemHelper;
import com.refinedmods.refinedstorage2.platform.api.item.NetworkBoundItemSession;
import com.refinedmods.refinedstorage2.platform.api.network.node.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.common.containermenu.slot.PlayerSlotReference;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class NetworkBoundItemHelperImpl implements NetworkBoundItemHelper {
    private static final MutableComponent UNBOUND = createTranslation("item", "network_item.unbound")
        .withStyle(ChatFormatting.RED);
    private static final Component UNBOUND_HELP = createTranslation("item", "network_item.unbound.help");

    private static final String TAG_X = "x";
    private static final String TAG_Y = "y";
    private static final String TAG_Z = "z";
    private static final String TAG_DIMENSION = "dim";

    @Override
    public boolean isBound(final ItemStack stack) {
        final CompoundTag tag = stack.getTag();
        if (tag == null) {
            return false;
        }
        return tag.contains(TAG_X) && tag.contains(TAG_Y) && tag.contains(TAG_Z) && tag.contains(TAG_DIMENSION);
    }

    @Override
    public void addTooltip(final ItemStack stack, final List<Component> lines) {
        getNetworkReference(stack).ifPresentOrElse(
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
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        if (isBound(stack)) {
            return Optional.empty();
        }
        return Optional.of(new HelpTooltipComponent(UNBOUND_HELP));
    }

    @Override
    public NetworkBoundItemSession openSession(final ItemStack stack,
                                               final ServerPlayer player,
                                               final InteractionHand hand) {
        final Optional<NetworkBoundItemSessionImpl.NetworkReference> networkReference = getNetworkReference(stack);
        return new NetworkBoundItemSessionImpl(
            player,
            PlayerSlotReference.of(player, hand),
            networkReference.orElse(null)
        );
    }

    private Optional<NetworkBoundItemSessionImpl.NetworkReference> getNetworkReference(final ItemStack stack) {
        if (!isBound(stack)) {
            return Optional.empty();
        }
        final CompoundTag tag = Objects.requireNonNull(stack.getTag());
        final int x = tag.getInt(TAG_X);
        final int y = tag.getInt(TAG_Y);
        final int z = tag.getInt(TAG_Z);
        final ResourceLocation dimension = new ResourceLocation(tag.getString(TAG_DIMENSION));
        return Optional.of(new NetworkBoundItemSessionImpl.NetworkReference(
            ResourceKey.create(Registries.DIMENSION, dimension),
            new BlockPos(x, y, z)
        ));
    }
}
