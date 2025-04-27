package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalCableBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.ColorableBlock;
import com.refinedmods.refinedstorage.common.support.DirectionalCableBlockShapes;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageBlock extends AbstractDirectionalCableBlock
    implements ColorableBlock<ExternalStorageBlock, BaseBlockItem>, EntityBlock, BlockItemProvider<BaseBlockItem> {
    private static final Component HELP = createTranslation("item", "external_storage.help");
    private static final ConcurrentHashMap<DirectionalCacheShapeCacheKey, VoxelShape> SHAPE_CACHE =
        new ConcurrentHashMap<>();
    private static final AbstractBlockEntityTicker<AbstractExternalStorageBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getExternalStorage);

    private final Identifier id;
    private final DyeColor color;
    private final MutableComponent name;
    private final BlockEntityProvider<AbstractExternalStorageBlockEntity> blockEntityProvider;

    public ExternalStorageBlock(final Identifier id,
                                final DyeColor color,
                                final MutableComponent name,
                                final BlockEntityProvider<AbstractExternalStorageBlockEntity> blockEntityProvider) {
        super(id, SHAPE_CACHE);
        this.id = id;
        this.color = color;
        this.name = name;
        this.blockEntityProvider = blockEntityProvider;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityProvider.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    protected void neighborChanged(final BlockState state, final Level level, final BlockPos pos, final Block block,
                                   @Nullable final Orientation orientation, final boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (level instanceof ServerLevel serverLevel
            && level.getBlockEntity(pos) instanceof AbstractExternalStorageBlockEntity blockEntity) {
            final boolean placedSomething = block instanceof AirBlock;
            if (placedSomething) {
                blockEntity.loadStorage(serverLevel);
            } else {
                blockEntity.neighborChanged();
            }
        }
    }

    @Override
    public BlockColorMap<ExternalStorageBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getExternalStorage();
    }

    @Override
    protected VoxelShape getExtensionShape(final Direction direction) {
        return switch (direction) {
            case NORTH -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_NORTH;
            case EAST -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_EAST;
            case SOUTH -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_SOUTH;
            case WEST -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_WEST;
            case UP -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_UP;
            case DOWN -> DirectionalCableBlockShapes.EXTERNAL_STORAGE_DOWN;
        };
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }
}
