package com.refinedmods.refinedstorage2.platform.common.support.network;

import com.refinedmods.refinedstorage2.api.network.impl.storage.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;

import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class NetworkNodeBlockEntityTicker<
    N extends AbstractNetworkNode,
    T extends NetworkNodeContainerBlockEntityImpl<N>
    > extends AbstractBlockEntityTicker<T> {
    @Nullable
    private final BooleanProperty activenessProperty;

    public NetworkNodeBlockEntityTicker(final Supplier<BlockEntityType<T>> allowedTypeSupplier) {
        super(allowedTypeSupplier);
        this.activenessProperty = null;
    }

    public NetworkNodeBlockEntityTicker(final Supplier<BlockEntityType<T>> allowedTypeSupplier,
                                        @Nullable final BooleanProperty activenessProperty) {
        super(allowedTypeSupplier);
        this.activenessProperty = activenessProperty;
    }

    @Override
    public void tick(final Level level, final BlockPos pos, final BlockState state, final T blockEntity) {
        blockEntity.updateActiveness(state, activenessProperty);
        blockEntity.doWork();
    }
}
