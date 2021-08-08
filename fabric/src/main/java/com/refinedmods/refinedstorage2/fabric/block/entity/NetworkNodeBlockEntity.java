package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerImpl;
import com.refinedmods.refinedstorage2.core.network.node.container.NetworkNodeContainerRepository;
import com.refinedmods.refinedstorage2.core.util.Direction;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.NetworkNodeBlock;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.container.FabricNetworkNodeContainerRepository;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NetworkNodeBlockEntity<T extends NetworkNodeImpl> extends BlockEntity implements NetworkNodeContainer<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    private long lastActiveChanged;
    private NbtCompound tag;
    private boolean mustInitialize;

    protected NetworkNodeContainerImpl<T> container;

    protected NetworkNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world != null && !world.isClient() && container != null) {
            if (mustInitialize) {
                container.initialize(new FabricNetworkNodeContainerRepository(world), Rs2Mod.API.getNetworkComponentRegistry());
                mustInitialize = false;
            } else {
                updateActivenessInWorld(state);
                container.getNode().update();
            }
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);

        if (world.isClient()) {
            return;
        }

        if (tag == null) {
            tag = new NbtCompound();
        }

        T node = createNode(world, pos, tag);
        this.container = createContainer(world, pos, node);
        this.mustInitialize = true;

        if (tag.contains(TAG_REDSTONE_MODE)) {
            container.getNode().setRedstoneMode(RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE)));
        }
    }

    protected NetworkNodeContainerImpl<T> createContainer(World world, BlockPos pos, T node) {
        return new NetworkNodeContainerImpl<>(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos), node);
    }

    protected abstract T createNode(World world, BlockPos pos, NbtCompound tag);

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.tag = nbt;
    }

    public void updateActivenessInWorld(BlockState state) {
        if (!state.contains(NetworkNodeBlock.ACTIVE)) {
            return;
        }

        boolean active = container.getNode().isActive();
        boolean activeInWorld = state.get(NetworkNodeBlock.ACTIVE);

        if (active != activeInWorld && (lastActiveChanged == 0 || System.currentTimeMillis() - lastActiveChanged > ACTIVE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Activeness state change for block at {}: {} -> {}", pos, activeInWorld, active);

            this.lastActiveChanged = System.currentTimeMillis();

            activenessChanged(active);
            updateActivenessInWorld(state, active);
        }
    }

    protected void activenessChanged(boolean active) {
    }

    private void updateActivenessInWorld(BlockState state, boolean active) {
        world.setBlockState(pos, state.with(NetworkNodeBlock.ACTIVE, active));
    }

    public RedstoneMode getRedstoneMode() {
        return container.getNode().getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        container.getNode().setRedstoneMode(redstoneMode);
        markDirty();
    }

    @Override
    public boolean initialize(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        return container.initialize(containerRepository, networkComponentRegistry);
    }

    @Override
    public void remove(NetworkNodeContainerRepository containerRepository, NetworkComponentRegistry networkComponentRegistry) {
        container.remove(containerRepository, networkComponentRegistry);
    }

    @Override
    public T getNode() {
        return container.getNode();
    }

    @Override
    public Rs2World getContainerWorld() {
        return container.getContainerWorld();
    }

    @Override
    public Position getPosition() {
        return container.getPosition();
    }

    @Override
    public List<NetworkNodeContainer<?>> getConnections(NetworkNodeContainerRepository containerRepository) {
        return container.getConnections(containerRepository);
    }

    @Override
    public boolean canConnectWith(NetworkNodeContainer<?> other, Direction incomingDirection) {
        return container.canConnectWith(other, incomingDirection);
    }
}
