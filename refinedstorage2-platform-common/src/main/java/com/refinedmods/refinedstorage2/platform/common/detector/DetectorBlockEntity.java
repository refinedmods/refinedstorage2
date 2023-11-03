package com.refinedmods.refinedstorage2.platform.common.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategyImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectorBlockEntity extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<DetectorNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DetectorBlockEntity.class);

    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_MODE = "mode";

    private final FilterWithFuzzyMode filter;
    private final RateLimiter poweredChangeRateLimiter = RateLimiter.create(1);

    private double amount;

    public DetectorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getDetector(), pos, state, new DetectorNetworkNode(
            Platform.INSTANCE.getConfig().getDetector().getEnergyUsage()
        ));
        final ResourceContainer resourceContainer = ResourceContainerImpl.createForFilter(1);
        this.filter = FilterWithFuzzyMode.createAndListenForTemplates(
            resourceContainer,
            () -> {
                propagateAmount();
                setChanged();
            },
            templates -> getNode().setFilterTemplate(
                templates.isEmpty() ? null : templates.get(0)
            )
        );
        initialize();
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        filter.save(tag);
        tag.putDouble(TAG_AMOUNT, amount);
        tag.putInt(TAG_MODE, DetectorModeSettings.getDetectorMode(getNode().getMode()));
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        filter.load(tag);
        if (tag.contains(TAG_AMOUNT)) {
            this.amount = tag.getDouble(TAG_AMOUNT);
        }
        if (tag.contains(TAG_MODE)) {
            getNode().setMode(DetectorModeSettings.getDetectorMode(tag.getInt(TAG_MODE)));
        }
        initialize();
        propagateAmount();
    }

    void setAmount(final double amount) {
        this.amount = amount;
        propagateAmount();
        setChanged();
    }

    private void propagateAmount() {
        final ResourceAmountTemplate<?> resourceAmount = filter.getFilterContainer().get(0);
        final long normalizedAmount = resourceAmount == null
            ? (long) amount
            : resourceAmount.getStorageChannelType().normalizeAmount(amount);
        LOGGER.debug("Updating detector amount of {} normalized as {}", amount, normalizedAmount);
        getNode().setAmount(normalizedAmount);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        initialize();
    }

    void setMode(final DetectorMode mode) {
        getNode().setMode(mode);
        setChanged();
    }

    DetectorMode getMode() {
        return getNode().getMode();
    }

    private void initialize() {
        final DetectorAmountStrategy defaultStrategy = new DetectorAmountStrategyImpl();
        final DetectorAmountStrategy strategy = isFuzzyMode()
            ? new FuzzyDetectorAmountStrategy(defaultStrategy)
            : defaultStrategy;
        getNode().setAmountStrategy(strategy);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        AbstractSingleAmountContainerMenu.writeToBuf(buf, amount, filter.getFilterContainer(), null);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.DETECTOR;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new DetectorContainerMenu(syncId, player, this, filter.getFilterContainer());
    }

    @Override
    public void updateActiveness(final BlockState state, @Nullable final BooleanProperty activenessProperty) {
        super.updateActiveness(state, activenessProperty);
        final boolean powered = getNode().isActive() && getNode().isActivated();
        final boolean needToUpdatePowered = state.getValue(DetectorBlock.POWERED) != powered;
        if (level != null && needToUpdatePowered && poweredChangeRateLimiter.tryAcquire()) {
            level.setBlockAndUpdate(getBlockPos(), state.setValue(DetectorBlock.POWERED, powered));
        }
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            return;
        }
        for (final Direction direction : Direction.values()) {
            if (direction == myDirection.getOpposite()) {
                continue;
            }
            sink.tryConnectInSameDimension(worldPosition.relative(direction), direction.getOpposite());
        }
    }

    @Override
    public boolean canAcceptIncomingConnection(final Direction incomingDirection, final BlockState connectingState) {
        if (!colorsAllowConnecting(connectingState)) {
            return false;
        }
        final Direction myDirection = getDirection();
        if (myDirection != null) {
            return myDirection != incomingDirection.getOpposite();
        }
        return true;
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}
