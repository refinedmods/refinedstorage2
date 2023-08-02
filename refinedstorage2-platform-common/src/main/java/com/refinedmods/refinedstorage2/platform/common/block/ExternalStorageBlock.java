package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.item.block.NamedBlockItem;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExternalStorageBlock extends AbstractDirectionalCableBlock
    implements ColorableBlock<ExternalStorageBlock>, EntityBlock {
    private static final Component HELP = createTranslation("item", "external_storage.help");
    private static final Map<DirectionalCacheShapeCacheKey, VoxelShape> SHAPE_CACHE = new HashMap<>();
    private static final AbstractBlockEntityTicker<ExternalStorageBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getExternalStorage);

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalStorageBlock.class);
    private final DyeColor color;
    private final MutableComponent name;

    public ExternalStorageBlock(final DyeColor color, final MutableComponent name) {
        super(SHAPE_CACHE);
        this.color = color;
        this.name = name;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ExternalStorageBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(final BlockState state,
                                final Level level,
                                final BlockPos pos,
                                final Block block,
                                final BlockPos fromPos,
                                final boolean moving) {
        super.neighborChanged(state, level, pos, block, fromPos, moving);
        if (level instanceof ServerLevel serverLevel
            && level.getBlockEntity(pos) instanceof ExternalStorageBlockEntity blockEntity) {
            LOGGER.debug("External storage neighbor has changed, reloading {}", pos);
            blockEntity.loadStorage(serverLevel);
        }
    }

    @Override
    public BlockColorMap<ExternalStorageBlock> getBlockColorMap() {
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

    public BlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), Blocks.INSTANCE.getExternalStorage().getName(
            getColor(),
            createTranslation("block", "external_storage")
        ), HELP);
    }
}
