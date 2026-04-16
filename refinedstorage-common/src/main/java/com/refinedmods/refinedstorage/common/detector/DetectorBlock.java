package com.refinedmods.refinedstorage.common.detector;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.ColorableBlock;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.DefaultDirectionType;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class DetectorBlock extends AbstractDirectionalBlock<Direction>
    implements ColorableBlock<DetectorBlock, BaseBlockItem>, SimpleWaterloggedBlock, EntityBlock,
    BlockItemProvider<BaseBlockItem> {
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

    private final Identifier id;
    private final DyeColor color;
    private final MutableComponent name;

    public DetectorBlock(final Identifier id, final DyeColor color, final MutableComponent name) {
        super(BlockProperties.stone(id));
        this.id = id;
        this.color = color;
        this.name = name;
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState()
            .setValue(BlockStateProperties.WATERLOGGED, false)
            .setValue(POWERED, false);
    }

    @Override
    protected boolean propagatesSkylightDown(final BlockState state) {
        return !state.getValue(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public FluidState getFluidState(final BlockState state) {
        return Boolean.TRUE.equals(state.getValue(BlockStateProperties.WATERLOGGED))
            ? Fluids.WATER.getSource(false)
            : super.getFluidState(state);
    }

    @Override
    protected DirectionType<Direction> getDirectionType() {
        return DefaultDirectionType.FACE_CLICKED;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
        builder.add(BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockColorMap<DetectorBlock, BaseBlockItem> getBlockColorMap() {
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
    @Nullable
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
        return Boolean.TRUE.equals(state.getValue(POWERED)) ? 15 : 0;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }
}
