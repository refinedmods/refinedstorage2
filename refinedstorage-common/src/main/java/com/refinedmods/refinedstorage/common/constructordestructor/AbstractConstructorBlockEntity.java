package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.constructordestructor.ConstructorStrategy;
import com.refinedmods.refinedstorage.common.api.constructordestructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.content.Items;
import com.refinedmods.refinedstorage.common.support.AbstractCableLikeBlockEntity;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
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

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class AbstractConstructorBlockEntity extends AbstractCableLikeBlockEntity<ConstructorNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ConstructorData> {
    private static final String TAG_DROP_ITEMS = "di";
    private static final String TAG_UPGRADES = "upgr";

    private final UpgradeContainer upgradeContainer;
    private final FilterWithFuzzyMode filter;
    private final SchedulingModeContainer schedulingModeContainer;

    private boolean dropItems;

    protected AbstractConstructorBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getConstructor(),
            pos,
            state,
            new ConstructorNetworkNode(Platform.INSTANCE.getConfig().getConstructor().getEnergyUsage())
        );
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.CONSTRUCTOR, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getConstructor().getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            if (level instanceof ServerLevel serverLevel) {
                initialize(serverLevel);
            }
        }, this::setChanged, ConstructorDestructorConstants.DEFAULT_WORK_TICK_RATE);
        this.ticker = upgradeContainer.getTicker();
        this.schedulingModeContainer = new SchedulingModeContainer(
            mainNetworkNode::setSchedulingMode,
            this::setChanged
        );
        this.filter = FilterWithFuzzyMode.createAndListenForFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
    }

    void setFilters(final List<ResourceKey> filters) {
        mainNetworkNode.setFilters(filters);
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
        mainNetworkNode.setPlayerProvider(() -> getFakePlayer(level));
        mainNetworkNode.setStrategy(createStrategy(level, direction));
    }

    private ConstructorStrategy createStrategy(final ServerLevel serverLevel, final Direction direction) {
        final Direction incomingDirection = direction.getOpposite();
        final BlockPos sourcePosition = worldPosition.relative(direction);
        final Collection<ConstructorStrategyFactory> factories = RefinedStorageApi.INSTANCE
            .getConstructorStrategyFactories();
        final List<ConstructorStrategy> strategies = factories.stream().flatMap(factory -> factory.create(
                serverLevel,
                sourcePosition,
                incomingDirection,
                upgradeContainer,
                dropItems
            ).stream())
            .toList();
        final ConstructorStrategy strategy = new CompositeConstructorStrategy(strategies);
        if (upgradeContainer.has(Items.INSTANCE.getAutocraftingUpgrade())) {
            return new AutocraftOnMissingResourcesConstructorStrategy(strategy);
        }
        return strategy;
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_UPGRADES, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(upgradeContainer.getUpgrades()));
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        input.read(TAG_UPGRADES, ItemContainerContents.CODEC).ifPresent(upgradeContainer::load);
        super.loadAdditional(input);
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        output.putBoolean(TAG_DROP_ITEMS, dropItems);
        schedulingModeContainer.store(output);
        filter.store(output);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        dropItems = input.getBooleanOr(TAG_DROP_ITEMS, false);
        schedulingModeContainer.read(input);
        filter.read(input);
    }

    @Override
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropContents(level, pos, upgradeContainer.getDrops());
        }
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

    boolean isDropItems() {
        return dropItems;
    }

    void setDropItems(final boolean dropItems) {
        this.dropItems = dropItems;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.CONSTRUCTOR);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new ConstructorContainerMenu(syncId, player, this, filter.getFilterContainer(), upgradeContainer,
            getExportingIndicators());
    }

    private ExportingIndicators getExportingIndicators() {
        return new ExportingIndicators(
            filter.getFilterContainer(),
            i -> toExportingIndicator(mainNetworkNode.getLastResult(i)),
            false
        );
    }

    private ExportingIndicator toExportingIndicator(final ConstructorStrategy.@Nullable Result result) {
        return switch (result) {
            case RESOURCE_MISSING -> ExportingIndicator.RESOURCE_MISSING;
            case AUTOCRAFTING_STARTED -> ExportingIndicator.AUTOCRAFTING_WAS_STARTED;
            case AUTOCRAFTING_MISSING_RESOURCES -> ExportingIndicator.AUTOCRAFTING_MISSING_RESOURCES;
            case null, default -> ExportingIndicator.NONE;
        };
    }

    @Override
    public ConstructorData getMenuData() {
        return new ConstructorData(ResourceContainerData.of(filter.getFilterContainer()),
            getExportingIndicators().getAll());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ConstructorData> getMenuCodec() {
        return ConstructorData.STREAM_CODEC;
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}
