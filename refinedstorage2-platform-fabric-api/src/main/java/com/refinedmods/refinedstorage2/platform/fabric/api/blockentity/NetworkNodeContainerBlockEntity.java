package com.refinedmods.refinedstorage2.platform.fabric.api.blockentity;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NetworkNodeContainerBlockEntity<T extends NetworkNode> extends BlockEntity implements NetworkNodeContainer {
    private final T node;

    protected NetworkNodeContainerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, T node) {
        super(type, pos, state);
        this.node = node;
    }

    // TODO: Consistency of these serverTick methods
    // TODO: Find better way to do initialization
    public static void serverTick(Level level, NetworkNodeContainerBlockEntity<?> blockEntity) {
        NetworkBuilder.INSTANCE.initialize(blockEntity, Rs2PlatformApiFacade.INSTANCE.createConnectionProvider(level), NetworkComponentRegistry.INSTANCE);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level.isClientSide) {
            return;
        }
        // TODO: Check here for chunk unloading.
        NetworkBuilder.INSTANCE.remove(this, Rs2PlatformApiFacade.INSTANCE.createConnectionProvider(level), NetworkComponentRegistry.INSTANCE);
    }

    @Override
    public T getNode() {
        return node;
    }
}
