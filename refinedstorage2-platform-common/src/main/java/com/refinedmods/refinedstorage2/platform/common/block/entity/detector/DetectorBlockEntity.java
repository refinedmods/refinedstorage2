package com.refinedmods.refinedstorage2.platform.common.block.entity.detector;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorAmountStrategyImpl;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.DetectorBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractInternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyModeBuilder;
import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DetectorBlockEntity extends AbstractInternalNetworkNodeContainerBlockEntity<DetectorNetworkNode>
    implements ExtendedMenuProvider {
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_MODE = "mode";

    private final FilterWithFuzzyMode filter;
    private final RateLimiter poweredChangeRateLimiter = RateLimiter.create(1);

    public DetectorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getDetector(), pos, state, new DetectorNetworkNode(
            Platform.INSTANCE.getConfig().getDetector().getEnergyUsage()
        ));
        this.filter = FilterWithFuzzyModeBuilder.of(1)
            .listener(this::setChanged)
            .templatesAcceptor(templates -> getNode().setFilterTemplate(
                templates.isEmpty() ? null : templates.get(0)
            ))
            .build();
        initialize();
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        filter.save(tag);
        tag.putLong(TAG_AMOUNT, getNode().getAmount());
        tag.putInt(TAG_MODE, DetectorModeSettings.getDetectorMode(getNode().getMode()));
    }

    @Override
    public void load(final CompoundTag tag) {
        filter.load(tag);
        if (tag.contains(TAG_AMOUNT)) {
            getNode().setAmount(tag.getLong(TAG_AMOUNT));
        }
        if (tag.contains(TAG_MODE)) {
            getNode().setMode(DetectorModeSettings.getDetectorMode(tag.getInt(TAG_MODE)));
        }
        initialize();
        super.load(tag);
    }

    public void setAmount(final long amount) {
        getNode().setAmount(amount);
        setChanged();
    }

    public long getAmount() {
        return getNode().getAmount();
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
        buf.writeLong(getNode().getAmount());
        filter.getFilterContainer().writeToUpdatePacket(buf);
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
