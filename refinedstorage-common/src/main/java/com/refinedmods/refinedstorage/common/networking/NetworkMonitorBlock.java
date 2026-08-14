package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NetworkMonitorBlock
    extends AbstractActiveColoredDirectionalBlock<OrientedDirection, NetworkMonitorBlock, BaseBlockItem>
    implements EntityBlock, BlockItemProvider<BaseBlockItem> {
    private static final AbstractBlockEntityTicker<NetworkMonitorBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getNetworkMonitor, ACTIVE);
    private static final Component HELP = createTranslation("item", "network_monitor.help");

    private final Identifier id;

    public NetworkMonitorBlock(final Identifier id, final DyeColor color, final MutableComponent name) {
        super(BlockProperties.stone(id), color, name);
        this.id = id;
    }

    @Override
    public BlockColorMap<NetworkMonitorBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getNetworkMonitor();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new NetworkMonitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return TICKER.get(level, type);
    }

    @Override
    protected DirectionType<OrientedDirection> getDirectionType() {
        return OrientedDirectionType.INSTANCE;
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }

    @Override
    protected boolean shouldChangedStateKeepBlockEntity(final BlockState oldState) {
        return oldState.getBlock() instanceof NetworkMonitorBlock;
    }
}
