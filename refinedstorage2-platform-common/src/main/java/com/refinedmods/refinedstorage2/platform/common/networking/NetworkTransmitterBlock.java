package com.refinedmods.refinedstorage2.platform.common.networking;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractColoredBlock;
import com.refinedmods.refinedstorage2.platform.common.support.NamedBlockItem;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class NetworkTransmitterBlock extends AbstractColoredBlock<NetworkTransmitterBlock> implements EntityBlock {
    public static final EnumProperty<NetworkTransmitterState> STATE = EnumProperty.create(
        "state",
        NetworkTransmitterState.class
    );

    private static final AbstractBlockEntityTicker<NetworkTransmitterBlockEntity> TICKER =
        new NetworkTransmitterBlockEntityTicker();

    public NetworkTransmitterBlock(final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES, color, name);
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(STATE, NetworkTransmitterState.INACTIVE);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATE);
    }

    @Override
    public BlockColorMap<NetworkTransmitterBlock, NamedBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getNetworkTransmitter();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new NetworkTransmitterBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return TICKER.get(level, type);
    }
}
