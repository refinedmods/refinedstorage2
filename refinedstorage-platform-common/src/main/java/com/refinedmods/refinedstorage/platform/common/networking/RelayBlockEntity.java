package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayComponentType;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayInputNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayOutputNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.storage.AccessModeSettings;
import com.refinedmods.refinedstorage.platform.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static java.util.Objects.requireNonNull;

public class RelayBlockEntity extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<RelayInputNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData> {
    private static final String TAG_PASS_THROUGH = "passthrough";
    private static final String TAG_PASS_ENERGY = "passenergy";
    private static final String TAG_PASS_SECURITY = "passsecurity";
    private static final String TAG_PASS_STORAGE = "passstorage";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_PRIORITY = "pri";

    private final FilterWithFuzzyMode filter;
    private final RelayOutputNetworkNode outputNode;

    private boolean passThrough = true;
    private FilterMode filterMode = FilterMode.BLOCK;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int priority = 0;

    public RelayBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getRelay(), pos, state, new RelayInputNetworkNode(
            Platform.INSTANCE.getConfig().getRelay().getInputNetworkEnergyUsage()
        ));
        this.outputNode = new RelayOutputNetworkNode(
            Platform.INSTANCE.getConfig().getRelay().getOutputNetworkEnergyUsage()
        );
        this.mainNode.setOutputNode(outputNode);
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::filterContainerChanged
        );
        this.mainNode.setFilterNormalizer(filter.createNormalizer());
        this.addContainer(new RelayOutputNetworkNodeContainer(this, outputNode));
        setRedstoneMode(RedstoneMode.LOW);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        final boolean wasActive = mainNode.isActive();
        // Updating fuzzy mode will call the filter's listener as the normalizer will yield different outputs.
        // However, when updating a filter the storage resets and "self-removes". If the normalizer yields different
        // outputs too early, the self-remove operation will partially fail as the expected resources will be different.
        // Therefore, we need to deactivate the node, update the fuzzy mode, and then reinitialize the node (ugly hack).
        mainNode.setActive(false);
        filter.setFuzzyMode(fuzzyMode);
        mainNode.setActive(wasActive);
    }

    private void filterContainerChanged(final Set<ResourceKey> filters) {
        mainNode.setFilters(filters);
        setChanged();
    }

    int getPriority() {
        return priority;
    }

    void setPriority(final int priority) {
        this.priority = priority;
        this.mainNode.setPriority(priority);
        setChanged();
    }

    AccessMode getAccessMode() {
        return accessMode;
    }

    void setAccessMode(final AccessMode accessMode) {
        this.accessMode = accessMode;
        mainNode.setAccessMode(accessMode);
        setChanged();
    }

    FilterMode getFilterMode() {
        return filterMode;
    }

    void setFilterMode(final FilterMode filterMode) {
        this.filterMode = filterMode;
        mainNode.setFilterMode(filterMode);
        setChanged();
    }

    boolean isPassEnergy() {
        return mainNode.hasComponentType(RelayComponentType.ENERGY);
    }

    void setPassEnergy(final boolean passEnergy) {
        mainNode.updateComponentType(RelayComponentType.ENERGY, passEnergy);
        setChanged();
    }

    boolean isPassStorage() {
        return mainNode.hasComponentType(RelayComponentType.STORAGE);
    }

    void setPassStorage(final boolean passStorage) {
        mainNode.updateComponentType(RelayComponentType.STORAGE, passStorage);
        setChanged();
    }

    boolean isPassSecurity() {
        return mainNode.hasComponentType(RelayComponentType.SECURITY);
    }

    void setPassSecurity(final boolean passSecurity) {
        mainNode.updateComponentType(RelayComponentType.SECURITY, passSecurity);
        setChanged();
    }

    boolean isPassThrough() {
        return passThrough;
    }

    void setPassThrough(final boolean passThrough) {
        this.passThrough = passThrough;
        this.mainNode.setComponentTypes(Set.of());
        setChanged();
        updateContainers();
    }

    boolean isActiveInternal() {
        return mainNode.isActive();
    }

    Direction getDirectionInternal() {
        return requireNonNull(getDirection());
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        outputNode.setActive(newActive);
        updateContainers();
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final RelayInputNetworkNode node) {
        return new RelayInputNetworkNodeContainer(this, node);
    }

    @Override
    public ResourceContainerData getMenuData() {
        return ResourceContainerData.of(filter.getFilterContainer());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ResourceContainerData> getMenuCodec() {
        return ResourceContainerData.STREAM_CODEC;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.RELAY;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new RelayContainerMenu(syncId, player, this, filter.getFilterContainer());
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        filter.save(tag, provider);
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(filterMode));
        tag.putBoolean(TAG_PASS_THROUGH, passThrough);
        tag.putBoolean(TAG_PASS_ENERGY, mainNode.hasComponentType(RelayComponentType.ENERGY));
        tag.putBoolean(TAG_PASS_STORAGE, mainNode.hasComponentType(RelayComponentType.STORAGE));
        tag.putBoolean(TAG_PASS_SECURITY, mainNode.hasComponentType(RelayComponentType.SECURITY));
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(accessMode));
        tag.putInt(TAG_PRIORITY, priority);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filter.load(tag, provider);
        if (tag.contains(TAG_FILTER_MODE)) {
            filterMode = FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE));
        }
        mainNode.setFilterMode(filterMode);
        if (tag.contains(TAG_PASS_THROUGH)) {
            passThrough = tag.getBoolean(TAG_PASS_THROUGH);
        }
        mainNode.setComponentTypes(getComponentTypes(tag));
        if (tag.contains(TAG_ACCESS_MODE)) {
            accessMode = AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE));
        }
        mainNode.setAccessMode(accessMode);
        if (tag.contains(TAG_PRIORITY)) {
            priority = tag.getInt(TAG_PRIORITY);
        }
        mainNode.setPriority(priority);
    }

    private Set<RelayComponentType> getComponentTypes(final CompoundTag tag) {
        final Set<RelayComponentType> types = new HashSet<>();
        if (tag.getBoolean(TAG_PASS_ENERGY)) {
            types.add(RelayComponentType.ENERGY);
        }
        if (tag.getBoolean(TAG_PASS_SECURITY)) {
            types.add(RelayComponentType.SECURITY);
        }
        if (tag.getBoolean(TAG_PASS_STORAGE)) {
            types.add(RelayComponentType.STORAGE);
        }
        return types;
    }
}
