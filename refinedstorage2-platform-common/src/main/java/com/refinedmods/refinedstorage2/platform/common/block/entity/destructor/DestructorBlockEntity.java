package com.refinedmods.refinedstorage2.platform.common.block.entity.destructor;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.NetworkNodeActor;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.blockentity.destructor.DestructorStrategy;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AbstractUpgradeableNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.DestructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;

import java.util.List;
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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class DestructorBlockEntity extends AbstractUpgradeableNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider {
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
        this.actor = new NetworkNodeActor(getNode());
        this.filterWithFuzzyMode = FilterWithFuzzyMode.createAndListenForUniqueTemplates(
            ResourceContainer.createForFilter(),
            this::setChanged,
            filter::setTemplates
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

    public FilterMode getFilterMode() {
        return filter.getMode();
    }

    public void setFilterMode(final FilterMode mode) {
        filter.setMode(mode);
        setChanged();
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(filter.getMode()));
        tag.putBoolean(TAG_PICKUP_ITEMS, pickupItems);
        filterWithFuzzyMode.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        filterWithFuzzyMode.load(tag);
        if (tag.contains(TAG_FILTER_MODE)) {
            filter.setMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }
        if (tag.contains(TAG_PICKUP_ITEMS)) {
            pickupItems = tag.getBoolean(TAG_PICKUP_ITEMS);
        }
        super.load(tag);
    }

    @Override
    protected void setEnergyUsage(final long upgradeEnergyUsage) {
        final long baseEnergyUsage = Platform.INSTANCE.getConfig().getDestructor().getEnergyUsage();
        getNode().setEnergyUsage(baseEnergyUsage + upgradeEnergyUsage);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filterWithFuzzyMode.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "destructor");
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
            .flatMap(factory -> factory.create(level, pos, incomingDirection, this::hasUpgrade, pickupItems).stream())
            .toList();
        this.strategy = new CompositeDestructorStrategy(strategies);
    }

    @Override
    public void postDoWork() {
        if (strategy == null
            || getNode().getNetwork() == null
            || !getNode().isActive()
            || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        final Player fakePlayer = getFakePlayer(serverLevel);
        strategy.apply(filter, actor, getNode()::getNetwork, fakePlayer);
    }
}
