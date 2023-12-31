package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskStateChangeListener implements StateTrackedStorage.Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiskStateChangeListener.class);

    private final BlockEntity blockEntity;
    private final RateLimiter rateLimiter = RateLimiter.create(1);

    private boolean syncRequested;

    public DiskStateChangeListener(final BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public void onStorageStateChanged() {
        syncRequested = true;
    }

    public void updateIfNecessary() {
        if (!syncRequested) {
            return;
        }
        if (!rateLimiter.tryAcquire()) {
            return;
        }
        LOGGER.debug("Disk state change for block at {}", blockEntity.getBlockPos());
        syncRequested = false;
        immediateUpdate();
    }

    public void immediateUpdate() {
        final Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }
        level.sendBlockUpdated(
            blockEntity.getBlockPos(),
            blockEntity.getBlockState(),
            blockEntity.getBlockState(),
            Block.UPDATE_ALL
        );
    }
}
