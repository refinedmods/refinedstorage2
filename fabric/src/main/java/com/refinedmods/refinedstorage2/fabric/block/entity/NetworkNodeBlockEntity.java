package com.refinedmods.refinedstorage2.fabric.block.entity;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostImpl;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostRepository;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostVisitor;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHostVisitorOperator;
import com.refinedmods.refinedstorage2.core.network.node.NetworkNodeImpl;
import com.refinedmods.refinedstorage2.core.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.core.util.Position;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.NetworkNodeBlock;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.host.FabricNetworkNodeHostRepository;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class NetworkNodeBlockEntity<T extends NetworkNodeImpl> extends BlockEntity implements NetworkNodeHost<T>, Tickable, NetworkNodeHostVisitor {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int ACTIVE_CHANGE_MINIMUM_INTERVAL_MS = 1000;
    private static final String TAG_REDSTONE_MODE = "rm";

    private long lastActiveChanged;
    private CompoundTag tag;
    protected BlockState cachedState;
    private boolean mustInitialize;

    protected NetworkNodeHostImpl<T> host;

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

        this.host = new NetworkNodeHostImpl<>(FabricRs2WorldAdapter.of(world), Positions.ofBlockPos(pos), createNode(world, pos, tag));
        this.mustInitialize = true;

        if (tag.contains(TAG_REDSTONE_MODE)) {
            host.getNode().setRedstoneMode(RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE)));
        }
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
        if (world != null && !world.isClient() && host != null) {
            if (mustInitialize) {
                host.initialize(new FabricNetworkNodeHostRepository(world), Rs2Mod.API.getNetworkComponentRegistry());
                mustInitialize = false;
            } else {
                updateActivenessInWorld();
                host.getNode().update();
            }
        }
    }

    private void updateActivenessInWorld() {
        calculateCachedStateIfNecessary();

        if (!cachedState.contains(NetworkNodeBlock.ACTIVE)) {
            return;
        }

        boolean active = host.getNode().isActive();
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
        return host.getNode().getRedstoneMode();
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        host.getNode().setRedstoneMode(redstoneMode);
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
    public boolean initialize(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        return host.initialize(hostRepository, networkComponentRegistry);
    }

    @Override
    public void remove(NetworkNodeHostRepository hostRepository, NetworkComponentRegistry networkComponentRegistry) {
        host.remove(hostRepository, networkComponentRegistry);
    }

    @Override
    public T getNode() {
        return host.getNode();
    }

    @Override
    public Rs2World getHostWorld() {
        return host.getHostWorld();
    }

    @Override
    public Position getPosition() {
        return host.getPosition();
    }

    @Override
    public void visit(NetworkNodeHostVisitorOperator operator) {
        host.visit(operator);
    }
}
