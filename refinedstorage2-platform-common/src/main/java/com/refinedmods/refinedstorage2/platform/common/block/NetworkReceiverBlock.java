package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.SimpleNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.item.block.NamedBlockItem;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class NetworkReceiverBlock extends AbstractBaseBlock implements ColorableBlock<NetworkReceiverBlock>,
    BlockItemProvider, EntityBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private static final AbstractBlockEntityTicker<SimpleNetworkNodeContainerBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getNetworkReceiver, ACTIVE);

    private final DyeColor color;
    private final MutableComponent name;

    public NetworkReceiverBlock(final DyeColor color, final MutableComponent name) {
        super(BlockConstants.PROPERTIES);
        this.color = color;
        this.name = name;
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
    public MutableComponent getName() {
        return name;
    }

    @Override
    public BlockColorMap<NetworkReceiverBlock> getBlockColorMap() {
        return Blocks.INSTANCE.getNetworkReceiver();
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public BlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), name);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new SimpleNetworkNodeContainerBlockEntity(
            BlockEntities.INSTANCE.getNetworkReceiver(),
            pos,
            state,
            Platform.INSTANCE.getConfig().getNetworkReceiver().getEnergyUsage()
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
