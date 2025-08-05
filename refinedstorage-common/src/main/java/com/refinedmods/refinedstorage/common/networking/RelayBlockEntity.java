package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayComponentType;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayInputNetworkNode;
import com.refinedmods.refinedstorage.api.network.impl.node.relay.RelayOutputNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.filter.FilterMode;
import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.storage.AccessModeSettings;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.FilterModeSettings;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;

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

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;
import static java.util.Objects.requireNonNull;

public class RelayBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<RelayInputNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData> {
    private static final String TAG_PASS_THROUGH = "passthrough";
    private static final String TAG_PASS_ENERGY = "passenergy";
    private static final String TAG_PASS_SECURITY = "passsecurity";
    private static final String TAG_PASS_STORAGE = "passstorage";
    private static final String TAG_PASS_AUTOCRAFTING = "passautocrafting";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_INSERT_PRIORITY = "pri";
    private static final String TAG_EXTRACT_PRIORITY = "epri";

    private final FilterWithFuzzyMode filter;
    private final RelayOutputNetworkNode outputNode;

    private boolean passThrough = true;
    private FilterMode filterMode = FilterMode.BLOCK;
    private AccessMode accessMode = AccessMode.INSERT_EXTRACT;
    private int insertPriority = 0;
    private int extractPriority = 0;

    public RelayBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getRelay(), pos, state, new RelayInputNetworkNode(
            Platform.INSTANCE.getConfig().getRelay().getInputNetworkEnergyUsage()
        ));
        this.outputNode = new RelayOutputNetworkNode(
            Platform.INSTANCE.getConfig().getRelay().getOutputNetworkEnergyUsage()
        );
        this.mainNetworkNode.setOutputNode(outputNode);
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
        this.mainNetworkNode.setFilterNormalizer(filter.createNormalizer());
        this.containers.addContainer(
            RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, outputNode)
                .name("output")
                .connectionStrategy(new RelayOutputConnectionStrategy(this))
                .build()
        );
        setRedstoneMode(RedstoneMode.LOW);
    }

    @Override
    public void doWork() {
        super.doWork();
        ticker.tick(outputNode);
    }

    boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    void setFuzzyMode(final boolean fuzzyMode) {
        final boolean wasActive = mainNetworkNode.isActive();
        // Updating fuzzy mode will call the filter's listener as the normalizer will yield different outputs.
        // However, when updating a filter the storage resets and "self-removes". If the normalizer yields different
        // outputs too early, the self-remove operation will partially fail as the expected resources will be different.
        // Therefore, we need to deactivate the node, update the fuzzy mode, and then reinitialize the node (ugly hack).
        mainNetworkNode.setActive(false);
        filter.setFuzzyMode(fuzzyMode);
        mainNetworkNode.setActive(wasActive);
    }

    void setFilters(final Set<ResourceKey> filters) {
        mainNetworkNode.setFilters(filters);
        setChanged();
    }

    int getInsertPriority() {
        return insertPriority;
    }

    void setInsertPriority(final int insertPriority) {
        this.insertPriority = insertPriority;
        this.mainNetworkNode.setInsertPriority(insertPriority);
        setChanged();
    }

    int getExtractPriority() {
        return extractPriority;
    }

    void setExtractPriority(final int extractPriority) {
        this.extractPriority = extractPriority;
        this.mainNetworkNode.setExtractPriority(extractPriority);
        setChanged();
    }

    AccessMode getAccessMode() {
        return accessMode;
    }

    void setAccessMode(final AccessMode accessMode) {
        this.accessMode = accessMode;
        mainNetworkNode.setAccessMode(accessMode);
        setChanged();
    }

    FilterMode getFilterMode() {
        return filterMode;
    }

    void setFilterMode(final FilterMode filterMode) {
        this.filterMode = filterMode;
        mainNetworkNode.setFilterMode(filterMode);
        setChanged();
    }

    boolean isPassEnergy() {
        return mainNetworkNode.hasComponentType(RelayComponentType.ENERGY);
    }

    void setPassEnergy(final boolean passEnergy) {
        mainNetworkNode.updateComponentType(RelayComponentType.ENERGY, passEnergy);
        setChanged();
    }

    boolean isPassStorage() {
        return mainNetworkNode.hasComponentType(RelayComponentType.STORAGE);
    }

    void setPassStorage(final boolean passStorage) {
        mainNetworkNode.updateComponentType(RelayComponentType.STORAGE, passStorage);
        setChanged();
    }

    boolean isPassSecurity() {
        return mainNetworkNode.hasComponentType(RelayComponentType.SECURITY);
    }

    void setPassSecurity(final boolean passSecurity) {
        mainNetworkNode.updateComponentType(RelayComponentType.SECURITY, passSecurity);
        setChanged();
    }

    boolean isPassAutocrafting() {
        return mainNetworkNode.hasComponentType(RelayComponentType.AUTOCRAFTING);
    }

    void setPassAutocrafting(final boolean passAutocrafting) {
        mainNetworkNode.updateComponentType(RelayComponentType.AUTOCRAFTING, passAutocrafting);
        setChanged();
    }

    boolean isPassThrough() {
        return passThrough;
    }

    void setPassThrough(final boolean passThrough) {
        this.passThrough = passThrough;
        this.mainNetworkNode.setComponentTypes(Set.of());
        setChanged();
        containers.update(level);
    }

    boolean isActiveInternal() {
        return mainNetworkNode.isActive();
    }

    Direction getDirectionInternal() {
        return requireNonNull(tryExtractDirection(getBlockState()));
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        outputNode.setActive(newActive);
        containers.update(level);
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final RelayInputNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .name("input")
            .connectionStrategy(new RelayInputConnectionStrategy(this))
            .build();
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
    public Component getName() {
        return overrideName(ContentNames.RELAY);
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
        tag.putBoolean(TAG_PASS_ENERGY, mainNetworkNode.hasComponentType(RelayComponentType.ENERGY));
        tag.putBoolean(TAG_PASS_STORAGE, mainNetworkNode.hasComponentType(RelayComponentType.STORAGE));
        tag.putBoolean(TAG_PASS_SECURITY, mainNetworkNode.hasComponentType(RelayComponentType.SECURITY));
        tag.putBoolean(TAG_PASS_AUTOCRAFTING, mainNetworkNode.hasComponentType(RelayComponentType.AUTOCRAFTING));
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(accessMode));
        tag.putInt(TAG_INSERT_PRIORITY, insertPriority);
        tag.putInt(TAG_EXTRACT_PRIORITY, extractPriority);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filter.load(tag, provider);
        if (tag.contains(TAG_FILTER_MODE)) {
            filterMode = FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE));
        }
        mainNetworkNode.setFilterMode(filterMode);
        if (tag.contains(TAG_PASS_THROUGH)) {
            passThrough = tag.getBoolean(TAG_PASS_THROUGH);
        }
        mainNetworkNode.setComponentTypes(getComponentTypes(tag));
        if (tag.contains(TAG_ACCESS_MODE)) {
            accessMode = AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE));
        }
        mainNetworkNode.setAccessMode(accessMode);
        if (tag.contains(TAG_INSERT_PRIORITY)) {
            insertPriority = tag.getInt(TAG_INSERT_PRIORITY);
        }
        mainNetworkNode.setInsertPriority(insertPriority);
        if (tag.contains(TAG_EXTRACT_PRIORITY)) {
            extractPriority = tag.getInt(TAG_EXTRACT_PRIORITY);
        } else {
            extractPriority = insertPriority; // bit of compat
        }
        mainNetworkNode.setExtractPriority(extractPriority);
    }

    private Set<RelayComponentType<?>> getComponentTypes(final CompoundTag tag) {
        final Set<RelayComponentType<?>> types = new HashSet<>();
        if (tag.getBoolean(TAG_PASS_ENERGY)) {
            types.add(RelayComponentType.ENERGY);
        }
        if (tag.getBoolean(TAG_PASS_SECURITY)) {
            types.add(RelayComponentType.SECURITY);
        }
        if (tag.getBoolean(TAG_PASS_STORAGE)) {
            types.add(RelayComponentType.STORAGE);
        }
        if (tag.getBoolean(TAG_PASS_AUTOCRAFTING)) {
            types.add(RelayComponentType.AUTOCRAFTING);
        }
        return types;
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }
}
