package com.refinedmods.refinedstorage.platform.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferNetworkNode;
import com.refinedmods.refinedstorage.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.storage.DiskContainerBlockEntityTicker;
import com.refinedmods.refinedstorage.platform.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.platform.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.platform.common.support.direction.DirectionType;

import java.util.function.BiFunction;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class DiskInterfaceBlock
    extends AbstractActiveColoredDirectionalBlock<BiDirection, DiskInterfaceBlock, BaseBlockItem>
    implements EntityBlock, BlockItemProvider<BaseBlockItem> {
    private static final Component HELP = createTranslation("item", "disk_interface.help");
    private static final DiskContainerBlockEntityTicker<StorageTransferNetworkNode, AbstractDiskInterfaceBlockEntity>
        TICKER = new DiskContainerBlockEntityTicker<>(BlockEntities.INSTANCE::getDiskInterface, ACTIVE);

    private final BiFunction<BlockPos, BlockState, AbstractDiskInterfaceBlockEntity> blockEntityFactory;

    public DiskInterfaceBlock(final DyeColor color,
                              final MutableComponent name,
                              final BiFunction<BlockPos, BlockState, AbstractDiskInterfaceBlockEntity>
                                  blockEntityFactory) {
        super(BlockConstants.PROPERTIES, color, name);
        this.blockEntityFactory = blockEntityFactory;
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return TICKER.get(level, type);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return blockEntityFactory.apply(blockPos, blockState);
    }

    @Override
    public BlockColorMap<DiskInterfaceBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getDiskInterface();
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }
}
