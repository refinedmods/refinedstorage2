package com.refinedmods.refinedstorage2.platform.fabric.api.block.entity;

import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.api.network.node.container.ConnectionProvider;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.container.PlatformNetworkNodeContainerImpl;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class NetworkNodeContainerBlockEntityImpl<T extends NetworkNodeImpl, C extends PlatformNetworkNodeContainer<T>> extends BlockEntity implements NetworkNodeContainerBlockEntity<T> {
    private C container;

    public NetworkNodeContainerBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (!world.isClient()) {
            if (container == null) {
                T node = createNode(pos, null);
                node.setActive(false);
                container = createContainer(pos, node);
            }
            container.setContainerWorld(world);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        container = createContainer(pos, createNode(pos, nbt));
    }

    // TODO: What about chunk unloading?
    @Override
    public void markRemoved() {
        super.markRemoved();
        if (world != null && !world.isClient()) {
            container.remove(createConnectionProvider(), NetworkComponentRegistry.INSTANCE);
        }
    }

    protected ConnectionProvider createConnectionProvider() {
        return Rs2PlatformApiFacade.INSTANCE.createConnectionProvider(world);
    }

    protected C createContainer(BlockPos pos, T node) {
        return (C) new PlatformNetworkNodeContainerImpl<T>(node, pos);
    }

    protected abstract T createNode(BlockPos pos, NbtCompound tag);

    @Override
    public C getContainer() {
        return container;
    }
}
