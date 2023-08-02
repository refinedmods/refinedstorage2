package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.direction.DirectionType;
import com.refinedmods.refinedstorage2.platform.common.block.direction.DirectionTypeImpl;
import com.refinedmods.refinedstorage2.platform.common.block.entity.detector.DetectorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.item.block.NamedBlockItem;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DetectorBlock extends AbstractDirectionalBlock<Direction>
    implements ColorableBlock<DetectorBlock>, SimpleWaterloggedBlock, EntityBlock, BlockItemProvider {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    private static final Component HELP = createTranslation("item", "detector.help");

    private static final AbstractBlockEntityTicker<DetectorBlockEntity> TICKER = new NetworkNodeBlockEntityTicker<>(
        BlockEntities.INSTANCE::getDetector
    );

    private static final VoxelShape SHAPE_DOWN = box(0, 0, 0, 16, 5, 16);
    private static final VoxelShape SHAPE_UP = box(0, 11, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_NORTH = box(0, 0, 0, 16, 16, 5);
    private static final VoxelShape SHAPE_EAST = box(11, 0, 0, 16, 16, 16);
    private static final VoxelShape SHAPE_SOUTH = box(0, 0, 11, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = box(0, 0, 0, 5, 16, 16);

    private final DyeColor color;
    private final MutableComponent name;

    public DetectorBlock(final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES);
        this.color = color;
        this.name = name;
        registerDefaultState(this.getStateDefinition().any().setValue(POWERED, false));
    }

    @Override
    protected DirectionType<Direction> getDirectionType() {
        return DirectionTypeImpl.INSTANCE;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    public BlockColorMap<DetectorBlock> getBlockColorMap() {
        return Blocks.INSTANCE.getDetector();
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter world,
                               final BlockPos pos,
                               final CollisionContext context) {
        final Direction direction = getDirection(state);
        if (direction == null) {
            return SHAPE_DOWN;
        }
        return switch (direction) {
            case DOWN -> SHAPE_DOWN;
            case UP -> SHAPE_UP;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new DetectorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> blockEntityType) {
        return TICKER.get(level, blockEntityType);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(final BlockState state,
                         final BlockGetter world,
                         final BlockPos pos,
                         final Direction side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public BlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), name, HELP);
    }
}
