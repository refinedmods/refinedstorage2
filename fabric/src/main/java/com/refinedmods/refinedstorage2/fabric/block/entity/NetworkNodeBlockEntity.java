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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NetworkNodeBlockEntity<T extends NetworkNodeImpl> extends BlockEntity implements NetworkNodeContainer<T>, Tickable {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    private long lastActiveChanged;
    private CompoundTag tag;
    protected BlockState cachedState;
    private boolean mustInitialize;

    protected NetworkNodeContainerImpl<T> container;

    protected NetworkNodeBlockEntity(BlockEntityType<?> type) {
        super(type);
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);

        if (world.isClient()) {
            return;
        }

        if (tag == null) {
            tag = new CompoundTag();
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

    protected abstract T createNode(World world, BlockPos pos, CompoundTag tag);

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));

        return super.toTag(tag);
    }

    @Override
    public void fromTag(BlockState blockState, CompoundTag tag) {
        this.tag = tag;
        super.fromTag(blockState, tag);
    }

    @Override
    public void tick() {
        if (world != null && !world.isClient() && container != null) {
            if (mustInitialize) {
                container.initialize(new FabricNetworkNodeContainerRepository(world), Rs2Mod.API.getNetworkComponentRegistry());
                mustInitialize = false;
            } else {
                updateActivenessInWorld();
                container.getNode().update();
            }
        }
    }

    private void updateActivenessInWorld() {
        calculateCachedStateIfNecessary();

        if (!cachedState.contains(NetworkNodeBlock.ACTIVE)) {
            return;
        }

        boolean active = container.getNode().isActive();
        boolean activeInWorld = cachedState.get(NetworkNodeBlock.ACTIVE);

        if (active != activeInWorld && (lastActiveChanged == 0 || System.currentTimeMillis() - lastActiveChanged > ACTIVE_CHANGE_MINIMUM_INTERVAL_MS)) {
            LOGGER.info("Activeness state change for block at {}: {} -> {}", pos, activeInWorld, active);

            this.lastActiveChanged = System.currentTimeMillis();

            activenessChanged(active);
            updateActivenessInWorld(active);
        }
    }

    protected void activenessChanged(boolean active) {
    }

    private void updateActivenessInWorld(boolean active) {
        BlockState newState = world.getBlockState(pos).with(NetworkNodeBlock.ACTIVE, active);
        updateState(newState);
    }

    public RedstoneMode getRedstoneMode() {
        return container.getNode().getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        container.getNode().setRedstoneMode(redstoneMode);
        markDirty();
    }

    protected void updateState(BlockState newState) {
        world.setBlockState(pos, newState);
        calculateCachedState();
    }

    protected void calculateCachedStateIfNecessary() {
        if (cachedState == null) {
            calculateCachedState();
        }
    }

    private void calculateCachedState() {
        this.cachedState = world.getBlockState(pos);
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
