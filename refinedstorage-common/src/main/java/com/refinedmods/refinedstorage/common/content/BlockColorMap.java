package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.support.BlockItemProvider;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public class BlockColorMap<T extends Block & BlockItemProvider<I>, I extends BlockItem> extends ColorMap<T> {
    private final BlockFactory<T> blockFactory;
    private final MutableComponent baseName;

    public BlockColorMap(final BlockFactory<T> blockFactory,
                         final Identifier baseId,
                         final MutableComponent baseName,
                         final DyeColor defaultColor) {
        super(baseId, defaultColor);
        this.blockFactory = requireNonNull(blockFactory);
        this.baseName = requireNonNull(baseName);
    }

    public Optional<InteractionResult> updateColor(final BlockState state,
                                                   final ItemStack heldItem,
                                                   final Level level,
                                                   final BlockPos pos,
                                                   final Player player) {
        final DyeColor color = heldItem.get(DataComponents.DYE);
        if (color == null || state.getBlock().equals(get(color))) {
            return Optional.empty();
        }
        if (!level.isClientSide()) {
            updateColorOnServer(state, heldItem, level, pos, (ServerPlayer) player, color);
        }
        return Optional.of(InteractionResult.SUCCESS);
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
            final MutableComponent colorTranslation = Component.translatable("color.minecraft." + color.getName());
            // this is a translation because concatting color and item names hardcoded reads unnatural in Japanese
            return createTranslation("item", "color_and_item_name", colorTranslation, baseName);
        } else {
            return baseName;
        }
    }

    public void registerBlocks(final RegistryCallback<Block> callback) {
        putAll(color -> callback.register(
            getId(color),
            () -> blockFactory.createBlock(getId(color), color, getName(color))
        ));
    }

    public void registerItems(final RegistryCallback<Item> callback) {
        registerItems(callback, itemSupplier -> {
        });
    }

    public void registerItems(final RegistryCallback<Item> callback, final Consumer<Supplier<I>> acceptor) {
        forEach((color, id, block) -> {
            final Supplier<I> itemSupplier = () -> block.get().createBlockItem();
            acceptor.accept(callback.register(id, itemSupplier));
        });
    }
}
