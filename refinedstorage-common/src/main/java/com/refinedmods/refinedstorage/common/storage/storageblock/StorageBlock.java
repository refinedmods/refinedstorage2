package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.StorageBlockProvider;
import com.refinedmods.refinedstorage.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class StorageBlock<T extends BlockEntity> extends AbstractBaseBlock implements EntityBlock {
    private final AbstractBlockEntityTicker<T> ticker;
    private final StorageBlockProvider provider;

    @SuppressWarnings("unchecked")
    public StorageBlock(final Properties properties, final StorageBlockProvider provider) {
        super(properties);
        this.ticker = new AbstractBlockEntityTicker<>(() -> (BlockEntityType<T>) provider.getBlockEntityType()) {
            @Override
            public void tick(final Level level,
                             final BlockPos blockPos,
                             final BlockState blockState,
                             final BlockEntity blockEntity) {
                if (blockEntity instanceof AbstractBaseNetworkNodeContainerBlockEntity<?> networkNode) {
                    networkNode.updateActiveness(blockState, null);
                    networkNode.doWork();
                }
            }
        };
        this.provider = provider;
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return RefinedStorageApi.INSTANCE.createStorageBlockEntity(pos, state, provider);
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return ticker.get(level, type);
    }
}
