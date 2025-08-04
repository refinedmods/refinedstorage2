package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.support.network.NetworkNodeContainerProvider;
import com.refinedmods.refinedstorage.common.networking.CableConnections;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

public abstract class AbstractCableLikeBlockEntity<T extends AbstractNetworkNode>
    extends AbstractBaseNetworkNodeContainerBlockEntity<T> {
    protected CableConnections connections = CableConnections.NONE;

    protected AbstractCableLikeBlockEntity(final BlockEntityType<?> type,
                                           final BlockPos pos,
                                           final BlockState state,
                                           final T networkNode) {
        super(type, pos, state, networkNode);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        connections.writeToTag(tag);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        connections = CableConnections.fromTag(tag);
        if (level != null && level.isClientSide()) {
            Platform.INSTANCE.requestModelDataUpdateOnClient(level, getBlockPos(), true);
        }
    }

    public final void updateConnections() {
        this.connections = computeConnections(level, getBlockState(), worldPosition);
        setChanged();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        return connections.writeToTag(super.getUpdateTag(provider));
    }

    public CableConnections getConnections() {
        return connections;
    }

    private static CableConnections computeConnections(@Nullable final Level level,
                                                       final BlockState state,
                                                       final BlockPos pos) {
        if (level == null) {
            return CableConnections.NONE;
        }
        final Direction myDirection = tryExtractDirection(state);
        final boolean north = hasVisualConnection(state, level, pos, Direction.NORTH, myDirection);
        final boolean east = hasVisualConnection(state, level, pos, Direction.EAST, myDirection);
        final boolean south = hasVisualConnection(state, level, pos, Direction.SOUTH, myDirection);
        final boolean west = hasVisualConnection(state, level, pos, Direction.WEST, myDirection);
        final boolean up = hasVisualConnection(state, level, pos, Direction.UP, myDirection);
        final boolean down = hasVisualConnection(state, level, pos, Direction.DOWN, myDirection);
        return new CableConnections(north, east, south, west, up, down);
    }

    private static boolean hasVisualConnection(
        final BlockState blockState,
        final Level level,
        final BlockPos pos,
        final Direction direction,
        @Nullable final Direction blacklistedDirection
    ) {
        if (direction == blacklistedDirection) {
            return false;
        }
        final BlockPos offsetPos = pos.relative(direction);
        final NetworkNodeContainerProvider neighbor = Platform.INSTANCE.getContainerProvider(
            level,
            offsetPos,
            direction.getOpposite()
        );
        if (neighbor == null) {
            return false;
        }
        return neighbor.getContainers()
            .stream()
            .anyMatch(container -> container.canAcceptIncomingConnection(direction.getOpposite(), blockState));
    }

    @Override
    public void setBlockState(final BlockState newBlockState) {
        super.setBlockState(newBlockState);
        if (level != null && !level.isClientSide()) {
            updateConnections();
            PlatformUtil.sendBlockUpdateToClient(level, getBlockPos());
        }
    }
}
