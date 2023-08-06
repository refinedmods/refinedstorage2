package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.block.BlockItemProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockColorMap<T extends Block & BlockItemProvider> extends ColorMap<T> {
    private final BlockFactory<T> blockFactory;
    private final MutableComponent baseName;

    public BlockColorMap(final BlockFactory<T> blockFactory,
                         final ResourceLocation baseId,
                         final MutableComponent baseName,
                         final DyeColor defaultColor) {
        super(baseId, defaultColor);
        this.blockFactory = Objects.requireNonNull(blockFactory);
        this.baseName = Objects.requireNonNull(baseName);
    }

    public Optional<InteractionResult> updateColor(final BlockState state,
                                                   final ItemStack heldItem,
                                                   final Level level,
                                                   final BlockPos pos,
                                                   final Player player) {
        final DyeColor color = heldItem.getItem() instanceof DyeItem dye ? dye.getDyeColor() : null;
        if (color == null || state.getBlock().equals(get(color))) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            updateColorOnServer(state, heldItem, level, pos, (ServerPlayer) player, color);
        }
        return Optional.of(InteractionResult.sidedSuccess(level.isClientSide()));
    }

    private void updateColorOnServer(final BlockState state,
                                     final ItemStack heldItem,
                                     final Level level,
                                     final BlockPos pos,
                                     final ServerPlayer player,
                                     final DyeColor color) {
        final T newBlock = get(color);
        level.setBlockAndUpdate(pos, getNewState(newBlock, state));
        if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) {
            heldItem.shrink(1);
        }
    }

    public Block[] toArray() {
        return values().toArray(new Block[0]);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private BlockState getNewState(final Block newBlock, final BlockState oldState) {
        BlockState newState = newBlock.defaultBlockState();
        for (final Property<?> property : oldState.getProperties()) {
            if (newState.hasProperty(property)) {
                newState = newState.setValue((Property) property, oldState.getValue(property));
            }
        }
        return newState;
    }

    private MutableComponent getName(final DyeColor color) {
        if (color != defaultColor) {
            return Component.translatable("color.minecraft." + color.getName()).append(" ").append(baseName);
        } else {
            return baseName;
        }
    }

    public void registerBlocks(final RegistryCallback<Block> callback) {
        putAll(color -> callback.register(
            getId(color),
            () -> blockFactory.createBlock(color, getName(color))
        ));
    }

    public void registerItems(final RegistryCallback<Item> callback) {
        registerItems(callback, itemSupplier -> {
        });
    }

    public void registerItems(final RegistryCallback<Item> callback, final Consumer<Supplier<BlockItem>> acceptor) {
        forEach((color, id, block) -> {
            final Supplier<BlockItem> itemSupplier = () -> block.get().createBlockItem();
            acceptor.accept(callback.register(id, itemSupplier));
        });
    }
}
