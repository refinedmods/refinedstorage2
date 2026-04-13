package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractColoredBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
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
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NetworkTransmitterBlock extends AbstractColoredBlock<NetworkTransmitterBlock> implements EntityBlock {
    public static final EnumProperty<NetworkTransmitterState> STATE = EnumProperty.create(
        "state",
        NetworkTransmitterState.class
    );

    private static final Component HELP = createTranslation("item", "network_transmitter.help");

    private static final AbstractBlockEntityTicker<NetworkTransmitterBlockEntity> TICKER =
        new NetworkTransmitterBlockEntityTicker();

    private final Identifier id;

    public NetworkTransmitterBlock(final Identifier id, final DyeColor color, final MutableComponent name) {
        super(BlockProperties.stone(id), color, name);
        this.id = id;
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
    public BlockColorMap<NetworkTransmitterBlock, BaseBlockItem> getBlockColorMap() {
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

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }
}
