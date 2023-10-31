package com.refinedmods.refinedstorage2.platform.common.networking;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractColoredBlock;
import com.refinedmods.refinedstorage2.platform.common.support.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.support.NetworkNodeContainerBlockEntityImpl;

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
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class NetworkReceiverBlock extends AbstractColoredBlock<NetworkReceiverBlock> implements EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final AbstractBlockEntityTicker<NetworkNodeContainerBlockEntityImpl<SimpleNetworkNode>> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getNetworkReceiver, ACTIVE);

    public NetworkReceiverBlock(final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES, color, name);
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public BlockColorMap<NetworkReceiverBlock> getBlockColorMap() {
        return Blocks.INSTANCE.getNetworkReceiver();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new NetworkNodeContainerBlockEntityImpl<>(
            BlockEntities.INSTANCE.getNetworkReceiver(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getNetworkReceiver().getEnergyUsage())
        );
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return TICKER.get(level, type);
    }
}
