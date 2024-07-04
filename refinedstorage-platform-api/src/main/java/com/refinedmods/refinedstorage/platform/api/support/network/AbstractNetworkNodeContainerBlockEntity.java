package com.refinedmods.refinedstorage.platform.api.support.network;

import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public abstract class AbstractNetworkNodeContainerBlockEntity<T extends NetworkNode> extends BlockEntity
    implements NetworkNodeContainerBlockEntity, ConnectionLogic {
    protected static final String MAIN_CONTAINER_NAME = "main";

    protected final T mainNode;
    protected final InWorldNetworkNodeContainer mainContainer;

    @Nullable
    protected Runnable initializationCallback;

    private final Set<InWorldNetworkNodeContainer> containers = new HashSet<>();

    protected AbstractNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                                      final BlockPos pos,
                                                      final BlockState state,
                                                      final T mainNode) {
        super(type, pos, state);
        this.mainContainer = createMainContainer(mainNode);
        addContainer(mainContainer);
        this.mainNode = mainNode;
    }

    protected InWorldNetworkNodeContainer createMainContainer(final T node) {
        return PlatformApi.INSTANCE.createInWorldNetworkNodeContainer(
            this,
            node,
            MAIN_CONTAINER_NAME,
            0,
            this,
            null
        );
    }

    protected final void addContainer(final InWorldNetworkNodeContainer container) {
        containers.add(container);
    }

    protected final void updateContainers() {
        containers.forEach(container -> PlatformApi.INSTANCE.onNetworkNodeContainerUpdated(container, level));
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        for (final Direction direction : Direction.values()) {
            sink.tryConnectInSameDimension(worldPosition.relative(direction), direction.getOpposite());
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        return true;
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        containers.forEach(container -> PlatformApi.INSTANCE.onNetworkNodeContainerInitialized(
            container,
            level,
            initializationCallback
        ));
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        containers.forEach(container -> PlatformApi.INSTANCE.onNetworkNodeContainerRemoved(container, level));
    }

    @Override
    public Set<InWorldNetworkNodeContainer> getContainers() {
        return containers;
    }
}
