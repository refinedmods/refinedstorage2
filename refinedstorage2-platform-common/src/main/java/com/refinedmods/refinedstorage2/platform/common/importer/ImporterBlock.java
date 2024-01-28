package com.refinedmods.refinedstorage2.platform.common.importer;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalCableBlock;
import com.refinedmods.refinedstorage2.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage2.platform.common.support.ColorableBlock;
import com.refinedmods.refinedstorage2.platform.common.support.DirectionalCableBlockShapes;
import com.refinedmods.refinedstorage2.platform.common.support.NamedBlockItem;
import com.refinedmods.refinedstorage2.platform.common.support.network.NetworkNodeBlockEntityTicker;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ImporterBlock extends AbstractDirectionalCableBlock implements
    ColorableBlock<ImporterBlock, NamedBlockItem>, EntityBlock, BlockItemProvider<NamedBlockItem> {
    private static final Component HELP = createTranslation("item", "importer.help");
    private static final Map<DirectionalCacheShapeCacheKey, VoxelShape> SHAPE_CACHE = new HashMap<>();
    private static final AbstractBlockEntityTicker<ImporterBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getImporter);
    private final DyeColor color;
    private final MutableComponent name;

    public ImporterBlock(final DyeColor color, final MutableComponent name) {
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
        return new ImporterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    @Override
    public BlockColorMap<ImporterBlock, NamedBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getImporter();
    }

    @Override
    protected VoxelShape getExtensionShape(final Direction direction) {
        return switch (direction) {
            case NORTH -> DirectionalCableBlockShapes.IMPORTER_NORTH;
            case EAST -> DirectionalCableBlockShapes.IMPORTER_EAST;
            case SOUTH -> DirectionalCableBlockShapes.IMPORTER_SOUTH;
            case WEST -> DirectionalCableBlockShapes.IMPORTER_WEST;
            case UP -> DirectionalCableBlockShapes.IMPORTER_UP;
            case DOWN -> DirectionalCableBlockShapes.IMPORTER_DOWN;
        };
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    public NamedBlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), name, HELP);
    }
}
