package com.refinedmods.refinedstorage2.platform.common.block.entity.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategyImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.DetectorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractSingleAmountContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DetectorBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<DetectorNetworkNode>
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
        final ResourceContainer resourceContainer = ResourceContainer.createForFilter(1);
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
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        filter.save(tag);
        tag.putDouble(TAG_AMOUNT, amount);
        tag.putInt(TAG_MODE, DetectorModeSettings.getDetectorMode(getNode().getMode()));
    }

    @Override
    public void load(final CompoundTag tag) {
        filter.load(tag);
        if (tag.contains(TAG_AMOUNT)) {
            this.amount = tag.getDouble(TAG_AMOUNT);
        }
        if (tag.contains(TAG_MODE)) {
            getNode().setMode(DetectorModeSettings.getDetectorMode(tag.getInt(TAG_MODE)));
        }
        initialize();
        propagateAmount();
        super.load(tag);
    }

    public void setAmount(final double amount) {
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

    public double getAmount() {
        return amount;
    }

    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        initialize();
    }

    public void setMode(final DetectorMode mode) {
        getNode().setMode(mode);
        setChanged();
    }

    public DetectorMode getMode() {
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
        return createTranslation("block", "detector");
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
    public boolean canAcceptIncomingConnection(final Direction direction, final BlockState other) {
        if (!colorsAllowConnecting(other)) {
            return false;
        }
        final Direction myDirection = getDirection();
        if (myDirection != null) {
            return myDirection != direction;
        }
        return true;
    }

    @Override
    public boolean canPerformOutgoingConnection(final Direction direction) {
        final Direction myDirection = getDirection();
        if (myDirection == null) {
            return true;
        }
        return myDirection != direction.getOpposite();
    }
}
