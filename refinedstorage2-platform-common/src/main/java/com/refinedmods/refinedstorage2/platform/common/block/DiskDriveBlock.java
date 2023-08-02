package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.block.direction.DirectionType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.DiskDriveBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.item.block.SimpleBlockItem;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DiskDriveBlock extends AbstractDirectionalBlock<BiDirection> implements EntityBlock {
    private static final Component HELP = createTranslation("item", "disk_drive.help");
    private static final DiskDriveBlockEntityTicker TICKER = new DiskDriveBlockEntityTicker();

    private final BiFunction<BlockPos, BlockState, AbstractDiskDriveBlockEntity> blockEntityFactory;

    public DiskDriveBlock(final BiFunction<BlockPos, BlockState, AbstractDiskDriveBlockEntity> blockEntityFactory) {
        super(BlockConstants.PROPERTIES);
        this.blockEntityFactory = blockEntityFactory;
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityFactory.apply(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }

    public static BlockItem createBlockItem() {
        return new SimpleBlockItem(Blocks.INSTANCE.getDiskDrive(), HELP);
    }
}
