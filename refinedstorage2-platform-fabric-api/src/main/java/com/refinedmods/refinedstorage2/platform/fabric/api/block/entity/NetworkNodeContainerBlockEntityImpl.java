package com.refinedmods.refinedstorage2.platform.fabric.api.block.entity;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainerImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class NetworkNodeContainerBlockEntityImpl<T extends NetworkNodeImpl, C extends PlatformNetworkNodeContainer<T>> extends BlockEntity implements NetworkNodeContainerBlockEntity<T> {
    private C container;

    public NetworkNodeContainerBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            if (container == null) {
                T node = createNode(getBlockPos(), null);
                node.setActive(false);
                container = createContainer(getBlockPos(), node);
            }
            container.setContainerLevel(level);
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        container = createContainer(getBlockPos(), createNode(getBlockPos(), nbt));
    }

    // TODO: What about chunk unloading?
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            container.remove(createConnectionProvider(), NetworkComponentRegistry.INSTANCE);
        }
    }

    protected ConnectionProvider createConnectionProvider() {
        return Rs2PlatformApiFacade.INSTANCE.createConnectionProvider(level);
    }

    protected C createContainer(BlockPos pos, T node) {
        return (C) new PlatformNetworkNodeContainerImpl<T>(node, pos);
    }

    protected abstract T createNode(BlockPos pos, CompoundTag tag);

    @Override
    public C getContainer() {
        return container;
    }
}
