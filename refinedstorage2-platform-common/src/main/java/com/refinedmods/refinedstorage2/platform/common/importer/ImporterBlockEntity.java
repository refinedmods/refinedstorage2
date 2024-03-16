package com.refinedmods.refinedstorage2.platform.common.importer;

import com.refinedmods.refinedstorage2.api.network.impl.node.importer.ImporterNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterTransferStrategy;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractUpgradeableNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.upgrade.UpgradeDestinations;

import java.util.List;
import java.util.function.LongSupplier;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImporterBlockEntity
    extends AbstractUpgradeableNetworkNodeContainerBlockEntity<ImporterNetworkNode>
    implements AmountOverride, ExtendedMenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ImporterBlockEntity.class);

    private static final String TAG_FILTER_MODE = "fim";

    private final FilterWithFuzzyMode filter;

    public ImporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getImporter(),
            pos,
            state,
            new ImporterNetworkNode(0),
            UpgradeDestinations.IMPORTER
        );
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            filters -> getNode().setFilters(filters)
        );
        getNode().setNormalizer(filter.createNormalizer());
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        final List<ImporterTransferStrategy> strategies = createStrategies(level, direction);
        LOGGER.debug("Initialized importer at {} with strategies {}", worldPosition, strategies);
        getNode().setTransferStrategies(strategies);
    }

    private List<ImporterTransferStrategy> createStrategies(final ServerLevel serverLevel, final Direction direction) {
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.relative(direction);
        final List<ImporterTransferStrategyFactory> factories =
            PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().getAll();
        return factories
            .stream()
            .map(factory -> factory.create(serverLevel, sourcePosition, incomingDirection, upgradeContainer, this))
            .toList();
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getNode().getFilterMode()));
        filter.save(tag);
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        if (tag.contains(TAG_FILTER_MODE)) {
            getNode().setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
        filter.load(tag);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    FilterMode getFilterMode() {
        return getNode().getFilterMode();
    }

    void setFilterMode(final FilterMode mode) {
        getNode().setFilterMode(mode);
        setChanged();
    }

    @Override
    protected void setEnergyUsage(final long upgradeEnergyUsage) {
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getImporter().getEnergyUsage();
        getNode().setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.IMPORTER;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ImporterContainerMenu(syncId, player, this, filter.getFilterContainer(), upgradeContainer);
    }

    @Override
    public long overrideAmount(final ResourceKey resource,
                               final long amount,
                               final LongSupplier currentAmount) {
        if (!upgradeContainer.has(Items.INSTANCE.getRegulatorUpgrade())) {
            return amount;
        }
        return upgradeContainer.getRegulatedAmount(resource)
            .stream()
            .map(desiredAmount -> getAmountStillAvailableForImport(amount, currentAmount.getAsLong(), desiredAmount))
            .findFirst()
            .orElse(amount);
    }

    private long getAmountStillAvailableForImport(final long amount,
                                                  final long currentAmount,
                                                  final long desiredAmount) {
        final long stillAvailableToImport = currentAmount - desiredAmount;
        if (stillAvailableToImport <= 0) {
            return 0;
        }
        return Math.min(stillAvailableToImport, amount);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}
