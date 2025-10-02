package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractCableLikeBlockEntity;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeContainer;
import com.refinedmods.refinedstorage.common.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import java.util.List;
import java.util.Set;
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
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDestructorBlockEntity
    extends AbstractCableLikeBlockEntity<DestructorNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData>, BlockEntityWithDrops {
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_PICKUP_ITEMS = "pi";
    private static final String TAG_UPGRADES = "upgr";

    private final FilterWithFuzzyMode filterWithFuzzyMode;
    private final UpgradeContainer upgradeContainer;

    private boolean pickupItems;

    protected AbstractDestructorBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getDestructor(),
            pos,
            state,
            new DestructorNetworkNode(Platform.INSTANCE.getConfig().getDestructor().getEnergyUsage())
        );
        this.filterWithFuzzyMode = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
        this.upgradeContainer = new UpgradeContainer(UpgradeDestinations.DESTRUCTOR, (c, upgradeEnergyUsage) -> {
            final long baseEnergyUsage = Platform.INSTANCE.getConfig().getDestructor().getEnergyUsage();
            mainNetworkNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
            setChanged();
            if (level instanceof ServerLevel serverLevel) {
                initialize(serverLevel);
            }
        }, ConstructorDestructorConstants.DEFAULT_WORK_TICK_RATE);
        this.ticker = upgradeContainer.getTicker();
    }

    public Container getUpgradeContainer() {
        return upgradeContainer;
    }

    @Override
    public List<ItemStack> getUpgrades() {
        return upgradeContainer.getUpgrades();
    }

    @Override
    public boolean addUpgrade(final ItemStack upgradeStack) {
        return upgradeContainer.addUpgrade(upgradeStack);
    }

    public boolean isPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(final boolean pickupItems) {
        this.pickupItems = pickupItems;
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
    }

    void setFilters(final Set<ResourceKey> filters) {
        mainNetworkNode.setFilters(filters);
    }

    public FilterMode getFilterMode() {
        return mainNetworkNode.getFilterMode();
    }

    public void setFilterMode(final FilterMode mode) {
        mainNetworkNode.setFilterMode(mode);
        setChanged();
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
    public final NonNullList<ItemStack> getDrops() {
        return upgradeContainer.getDrops();
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(mainNetworkNode.getFilterMode()));
        tag.putBoolean(TAG_PICKUP_ITEMS, pickupItems);
        filterWithFuzzyMode.save(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filterWithFuzzyMode.load(tag, provider);
        if (tag.contains(TAG_FILTER_MODE)) {
            mainNetworkNode.setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
        if (tag.contains(TAG_PICKUP_ITEMS)) {
            pickupItems = tag.getBoolean(TAG_PICKUP_ITEMS);
        }
    }

    @Override
    public ResourceContainerData getMenuData() {
        return ResourceContainerData.of(filterWithFuzzyMode.getFilterContainer());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ResourceContainerData> getMenuCodec() {
        return ResourceContainerData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.DESTRUCTOR);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new DestructorContainerMenu(
            syncId,
            player,
            this,
            filterWithFuzzyMode.getFilterContainer(),
            upgradeContainer
        );
    }

    @Override
    protected void initialize(final ServerLevel level, final Direction direction) {
        super.initialize(level, direction);
        mainNetworkNode.setPlayerProvider(() -> getFakePlayer(level));
        mainNetworkNode.setStrategy(createStrategy(level, direction));
    }

    private CompositeDestructorStrategy createStrategy(final ServerLevel level,
                                                       final Direction direction) {
        final BlockPos pos = getBlockPos().relative(direction);
        final Direction incomingDirection = direction.getOpposite();
        final List<DestructorStrategy> strategies = RefinedStorageApi.INSTANCE.getDestructorStrategyFactories()
            .stream()
            .flatMap(factory -> factory.create(level, pos, incomingDirection, upgradeContainer, pickupItems).stream())
            .toList();
        return new CompositeDestructorStrategy(strategies);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}
