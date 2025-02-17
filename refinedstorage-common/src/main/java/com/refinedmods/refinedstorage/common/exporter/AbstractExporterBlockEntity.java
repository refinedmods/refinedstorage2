package com.refinedmods.refinedstorage.common.exporter;

import com.refinedmods.refinedstorage.api.network.impl.node.exporter.ExporterNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.SchedulingMode;
import com.refinedmods.refinedstorage.api.network.node.exporter.ExporterTransferStrategy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractCableLikeBlockEntity;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.SchedulingModeContainer;
import com.refinedmods.refinedstorage.common.support.SchedulingModeType;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicator;
import com.refinedmods.refinedstorage.common.support.exportingindicator.ExportingIndicators;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractExporterBlockEntity
    extends AbstractCableLikeBlockEntity<ExporterNetworkNode>
    implements BlockEntityWithDrops, NetworkNodeExtendedMenuProvider<ExporterData> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractExporterBlockEntity.class);
    private static final String TAG_UPGRADES = "upgr";

    private final UpgradeContainer upgradeContainer;
    private final FilterWithFuzzyMode filter;
    private final SchedulingModeContainer schedulingModeContainer;

    protected AbstractExporterBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getExporter(),
            pos,
            state,
            new ExporterNetworkNode(Platform.INSTANCE.getConfig().getExporter().getEnergyUsage())
        );
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.EXPORTER, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getExporter().getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            setChanged();
            if (level instanceof ServerLevel serverLevel) {
                initialize(serverLevel);
            }
        });
        this.ticker = upgradeContainer.getTicker();
        this.schedulingModeContainer = new SchedulingModeContainer(this::schedulingModeChanged);
        this.filter = FilterWithFuzzyMode.createAndListenForFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
    }

    private void schedulingModeChanged(final SchedulingMode schedulingMode) {
        mainNetworkNode.setSchedulingMode(schedulingMode);
        setChanged();
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return upgradeContainer.addUpgrade(upgradeStack);
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        super.initialize(level, direction);
        final ExporterTransferStrategy strategy = createStrategy(level, direction);
        LOGGER.debug("Initialized exporter at {} with strategy {}", worldPosition, strategy);
        mainNetworkNode.setTransferStrategy(strategy);
    }

    private ExporterTransferStrategy createStrategy(final ServerLevel serverLevel, final Direction direction) {
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.relative(direction);
        final List<ExporterTransferStrategyFactory> factories =
            RefinedStorageApi.INSTANCE.getExporterTransferStrategyRegistry().getAll();
        final Map<Class<? extends ResourceKey>, ExporterTransferStrategy> strategies =
            factories.stream().collect(Collectors.toMap(
                ExporterTransferStrategyFactory::getResourceType,
                factory -> factory.create(
                    serverLevel,
                    sourcePosition,
                    incomingDirection,
                    upgradeContainer,
                    filter.isFuzzyMode()
                )
            ));
        return new CompositeExporterTransferStrategy(strategies);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_UPGRADES, ContainerUtil.write(upgradeContainer, provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_UPGRADES)) {
            ContainerUtil.read(tag.getCompound(TAG_UPGRADES), upgradeContainer, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        schedulingModeContainer.writeToTag(tag);
        filter.save(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        schedulingModeContainer.loadFromTag(tag);
        filter.load(tag, provider);
    }

    void setSchedulingModeType(final SchedulingModeType type) {
        schedulingModeContainer.setType(type);
    }

    SchedulingModeType getSchedulingModeType() {
        return schedulingModeContainer.getType();
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
    }

    @Override
    public final NonNullList<ItemStack> getDrops() {
        return upgradeContainer.getDrops();
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.EXPORTER);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ExporterContainerMenu(syncId, player, this, filter.getFilterContainer(), upgradeContainer,
            getExportingIndicators());
    }

    private ExportingIndicators getExportingIndicators() {
        return new ExportingIndicators(
            filter.getFilterContainer(),
            i -> toExportingIndicator(mainNetworkNode.getLastResult(i)),
            false
        );
    }

    private ExportingIndicator toExportingIndicator(@Nullable final ExporterTransferStrategy.Result result) {
        return switch (result) {
            case DESTINATION_DOES_NOT_ACCEPT -> ExportingIndicator.DESTINATION_DOES_NOT_ACCEPT_RESOURCE;
            case RESOURCE_MISSING -> ExportingIndicator.RESOURCE_MISSING;
            case AUTOCRAFTING_STARTED -> ExportingIndicator.AUTOCRAFTING_WAS_STARTED;
            case AUTOCRAFTING_MISSING_RESOURCES -> ExportingIndicator.AUTOCRAFTING_MISSING_RESOURCES;
            case null, default -> ExportingIndicator.NONE;
        };
    }

    @Override
    public ExporterData getMenuData() {
        final ResourceContainer filterContainer = filter.getFilterContainer();
        final ResourceContainerData resourceContainerData = ResourceContainerData.of(filterContainer);
        return new ExporterData(resourceContainerData, getExportingIndicators().getAll());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ExporterData> getMenuCodec() {
        return ExporterData.STREAM_CODEC;
    }

    void setFilters(final List<ResourceKey> filters) {
        mainNetworkNode.setFilters(filters);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}
