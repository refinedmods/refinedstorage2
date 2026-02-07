package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferNetworkNode;
import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.storage.DiskContainerBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;

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

public class DiskInterfaceBlock
    extends AbstractActiveColoredDirectionalBlock<OrientedDirection, DiskInterfaceBlock, BaseBlockItem>
    implements EntityBlock, BlockItemProvider<BaseBlockItem> {
    private static final Component HELP = createTranslation("item", "disk_interface.help");
    private static final DiskContainerBlockEntityTicker<StorageTransferNetworkNode, AbstractDiskInterfaceBlockEntity>
        TICKER = new DiskContainerBlockEntityTicker<>(BlockEntities.INSTANCE::getDiskInterface, ACTIVE);

    private final Identifier id;
    private final BlockEntityProvider<AbstractDiskInterfaceBlockEntity> blockEntityProvider;

    public DiskInterfaceBlock(final Identifier id,
                              final DyeColor color,
                              final MutableComponent name,
                              final BlockEntityProvider<AbstractDiskInterfaceBlockEntity> blockEntityProvider) {
        super(BlockProperties.stone(id), color, name);
        this.id = id;
        this.blockEntityProvider = blockEntityProvider;
    }

    @Override
    protected DirectionType<OrientedDirection> getDirectionType() {
        return OrientedDirectionType.INSTANCE;
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
        return blockEntityProvider.create(blockPos, blockState);
    }

    @Override
    public BlockColorMap<DiskInterfaceBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getDiskInterface();
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
