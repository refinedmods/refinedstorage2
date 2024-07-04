package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.Filter;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategy;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractUpgradeableNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeDestinations;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

public class DestructorBlockEntity extends AbstractUpgradeableNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData> {
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_PICKUP_ITEMS = "pi";

    private final FilterWithFuzzyMode filterWithFuzzyMode;
    private final Filter filter = new Filter();
    private final Actor actor;
    @Nullable
    private DestructorStrategy strategy;
    private boolean pickupItems;

    public DestructorBlockEntity(final BlockPos pos, final BlockState state) {
        super(
            BlockEntities.INSTANCE.getDestructor(),
            pos,
            state,
            new SimpleNetworkNode(Platform.INSTANCE.getConfig().getDestructor().getEnergyUsage()),
            UpgradeDestinations.DESTRUCTOR
        );
        this.actor = new NetworkNodeActor(mainNode);
        this.filterWithFuzzyMode = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
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
        filter.setFilters(filters);
    }

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(final FilterMode mode) {
        filter.setMode(mode);
        setChanged();
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(filter.getMode()));
        tag.putBoolean(TAG_PICKUP_ITEMS, pickupItems);
        filterWithFuzzyMode.save(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filterWithFuzzyMode.load(tag, provider);
        if (tag.contains(TAG_FILTER_MODE)) {
            filter.setMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
        if (tag.contains(TAG_PICKUP_ITEMS)) {
            pickupItems = tag.getBoolean(TAG_PICKUP_ITEMS);
        }
    }

    @Override
    protected void setEnergyUsage(final long upgradeEnergyUsage) {
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getDestructor().getEnergyUsage();
        mainNode.setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
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
    public Component getDisplayName() {
        return ContentNames.DESTRUCTOR;
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
        final BlockPos pos = getBlockPos().relative(direction);
        final Direction incomingDirection = direction.getOpposite();
        final List<DestructorStrategy> strategies = PlatformApi.INSTANCE.getDestructorStrategyFactories()
            .stream()
            .flatMap(factory -> factory.create(level, pos, incomingDirection, upgradeContainer, pickupItems).stream())
            .toList();
        this.strategy = new CompositeDestructorStrategy(strategies);
    }

    @Override
    public void postDoWork() {
        if (strategy == null
            || mainNode.getNetwork() == null
            || !mainNode.isActive()
            || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Player fakePlayer = getFakePlayer(serverLevel);
        strategy.apply(filter, actor, mainNode::getNetwork, fakePlayer);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}
