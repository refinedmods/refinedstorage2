package com.refinedmods.refinedstorage2.platform.common.block.ticker;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class AbstractBlockEntityTicker<T extends BlockEntity> implements BlockEntityTicker<T> {
    private final Supplier<BlockEntityType<T>> allowedTypeSupplier;

    protected AbstractBlockEntityTicker(final Supplier<BlockEntityType<T>> allowedTypeSupplier) {
        this.allowedTypeSupplier = allowedTypeSupplier;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <O extends BlockEntity> AbstractBlockEntityTicker<O> get(final Level level, final BlockEntityType<O> type) {
        return !level.isClientSide && allowedTypeSupplier.get().equals(type)
            ? (AbstractBlockEntityTicker<O>) this
            : null;
    }
}
