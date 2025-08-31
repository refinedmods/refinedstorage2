package com.refinedmods.refinedstorage.common.detector;

import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorAmountStrategy;
import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorAmountStrategyImpl;
import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorMode;
import com.refinedmods.refinedstorage.api.network.impl.node.detector.DetectorNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.containermenu.SingleAmountData;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetectorBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<DetectorNetworkNode>
    implements NetworkNodeExtendedMenuProvider<SingleAmountData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DetectorBlockEntity.class);
    private static final int POWERED_CHANGE_TICK_RATE = 20;

    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_MODE = "mode";

    private final FilterWithFuzzyMode filter;
    private int poweredChangeTicks;

    private double amount;

    public DetectorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getDetector(), pos, state, new DetectorNetworkNode(
            Platform.INSTANCE.getConfig().getDetector().getEnergyUsage()
        ));
        final ResourceContainer resourceContainer = ResourceContainerImpl.createForFilter(1);
        this.filter = FilterWithFuzzyMode.createAndListenForFilters(
            resourceContainer,
            () -> {
                propagateAmount();
                setChanged();
            },
            filters -> mainNetworkNode.setConfiguredResource(filters.isEmpty() ? null : filters.getFirst())
        );
        initialize();
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final DetectorNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .connectionStrategy(new DetectorConnectionStrategy(this::getBlockState, getBlockPos()))
            .build();
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        filter.save(tag, provider);
        tag.putDouble(TAG_AMOUNT, amount);
        tag.putInt(TAG_MODE, DetectorModeSettings.getDetectorMode(mainNetworkNode.getMode()));
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filter.load(tag, provider);
        if (tag.contains(TAG_AMOUNT)) {
            this.amount = tag.getDouble(TAG_AMOUNT);
        }
        if (tag.contains(TAG_MODE)) {
            mainNetworkNode.setMode(DetectorModeSettings.getDetectorMode(tag.getInt(TAG_MODE)));
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
        final PlatformResourceKey configuredResource = filter.getFilterContainer().getResource(0);
        final long normalizedAmount = configuredResource == null
            ? (long) amount
            : configuredResource.getResourceType().normalizeAmount(amount);
        LOGGER.debug("Updating detector amount of {} normalized as {}", amount, normalizedAmount);
        mainNetworkNode.setAmount(normalizedAmount);
    }

    void setConfiguredResource(final ResourceKey configuredResource) {
        mainNetworkNode.setConfiguredResource(configuredResource);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        initialize();
    }

    void setMode(final DetectorMode mode) {
        mainNetworkNode.setMode(mode);
        setChanged();
    }

    DetectorMode getMode() {
        return mainNetworkNode.getMode();
    }

    private void initialize() {
        final DetectorAmountStrategy defaultStrategy = new DetectorAmountStrategyImpl();
        final DetectorAmountStrategy strategy = isFuzzyMode()
            ? new FuzzyDetectorAmountStrategy(defaultStrategy)
            : defaultStrategy;
        mainNetworkNode.setAmountStrategy(strategy);
    }

    @Override
    public SingleAmountData getMenuData() {
        return new SingleAmountData(
            Optional.empty(),
            amount,
            ResourceContainerData.of(filter.getFilterContainer())
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, SingleAmountData> getMenuCodec() {
        return SingleAmountData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.DETECTOR);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new DetectorContainerMenu(syncId, player, this, filter.getFilterContainer());
    }

    @Override
    public void updateActiveness(final BlockState state, @Nullable final BooleanProperty activenessProperty) {
        super.updateActiveness(state, activenessProperty);
        final boolean powered = mainNetworkNode.isActive() && mainNetworkNode.isActivated();
        final boolean needToUpdatePowered = state.getValue(DetectorBlock.POWERED) != powered;
        if (level != null && needToUpdatePowered && poweredChangeTicks++ % POWERED_CHANGE_TICK_RATE == 0) {
            level.setBlockAndUpdate(getBlockPos(), state.setValue(DetectorBlock.POWERED, powered));
            poweredChangeTicks = 0;
        }
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Override
    protected boolean hasRedstoneMode() {
        return false;
    }

}
