package com.refinedmods.refinedstorage.platform.common.wirelesstransmitter;

import com.refinedmods.refinedstorage.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.platform.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.platform.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.direction.DefaultDirectionType;
import com.refinedmods.refinedstorage.platform.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.platform.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class WirelessTransmitterBlock
    extends AbstractActiveColoredDirectionalBlock<Direction, WirelessTransmitterBlock, BaseBlockItem>
    implements BlockItemProvider<BaseBlockItem>, EntityBlock {
    private static final AbstractBlockEntityTicker<WirelessTransmitterBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getWirelessTransmitter, ACTIVE);
    private static final Component HELP = createTranslation("item", "wireless_transmitter.help");

    private static final VoxelShape SHAPE_DOWN = box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape SHAPE_UP = box(6.0D, 6.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final VoxelShape SHAPE_EAST = box(6.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);
    private static final VoxelShape SHAPE_WEST = box(0.0D, 6.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape SHAPE_NORTH = box(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape SHAPE_SOUTH = box(6.0D, 6.0D, 6.0D, 10.0D, 10.0D, 16.0D);

    public WirelessTransmitterBlock(final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES, color, name);
    }

    @Override
    protected DirectionType<Direction> getDirectionType() {
        return DefaultDirectionType.FACE_CLICKED;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(final BlockState state,
                               final BlockGetter world,
                               final BlockPos pos,
                               final CollisionContext context) {
        final Direction direction = getDirection(state);
        if (direction == null) {
            return Shapes.empty();
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
    public BlockColorMap<WirelessTransmitterBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getWirelessTransmitter();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new WirelessTransmitterBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return TICKER.get(level, type);
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }
}
