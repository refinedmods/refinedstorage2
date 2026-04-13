package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.common.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.common.support.ColorableBlock;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

public class ColoredConnectionStrategy extends SimpleConnectionStrategy {
    protected final Supplier<BlockState> blockStateProvider;

    public ColoredConnectionStrategy(final Supplier<BlockState> blockStateProvider, final BlockPos origin) {
        super(origin);
        this.blockStateProvider = blockStateProvider;
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        final Direction myDirection = tryExtractDirection(blockStateProvider.get());
        if (myDirection == null) {
            super.addOutgoingConnections(sink);
            return;
        }
        for (final Direction direction : Direction.values()) {
            if (direction == myDirection) {
                continue;
            }
            sink.tryConnectInSameDimension(origin.relative(direction), direction.getOpposite());
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        if (!colorsAllowConnecting(connectingState)) {
            return false;
        }
        final Direction myDirection = tryExtractDirection(blockStateProvider.get());
        if (myDirection != null) {
            return myDirection != incomingDirection;
        }
        return true;
    }

    protected final boolean colorsAllowConnecting(final BlockState connectingState) {
        if (!(connectingState.getBlock() instanceof ColorableBlock<?, ?> otherColorableBlock)) {
            return true;
        }
        final ColorableBlock<?, ?> colorableBlock = getColor();
        if (colorableBlock == null) {
            return true;
        }
        return otherColorableBlock.getColor() == colorableBlock.getColor()
            || colorableBlock.canAlwaysConnect()
            || otherColorableBlock.canAlwaysConnect();
    }

    @Nullable
    private ColorableBlock<?, ?> getColor() {
        if (!(blockStateProvider.get().getBlock() instanceof ColorableBlock<?, ?> colorableBlock)) {
            return null;
        }
        return colorableBlock;
    }
}
