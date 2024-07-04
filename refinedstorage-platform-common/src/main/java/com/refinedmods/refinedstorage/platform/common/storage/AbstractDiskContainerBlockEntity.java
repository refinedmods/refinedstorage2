package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.platform.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.platform.common.util.ContainerUtil;

import java.util.Set;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractDiskContainerBlockEntity<T extends AbstractStorageContainerNetworkNode>
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<T>
    implements BlockEntityWithDrops, NetworkNodeExtendedMenuProvider<ResourceContainerData> {
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "disks";

    protected final FilterWithFuzzyMode filter;
    protected final DiskInventory diskInventory;
    @Nullable
    protected Disk[] disks;

    private final DiskStateChangeListener diskStateListener = new DiskStateChangeListener(this);

    protected AbstractDiskContainerBlockEntity(final BlockEntityType<?> type,
                                               final BlockPos pos,
                                               final BlockState state,
                                               final T node) {
        super(type, pos, state, node);
        this.diskInventory = new DiskInventory((inventory, slot) -> onDiskChanged(slot), mainNode.getSize());
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
        this.mainNode.setListener(diskStateListener);
        setNormalizer(filter.createNormalizer());
        // It's important to sync here as the initial update packet might have failed as the network
        // could possibly be not initialized yet.
        this.initializationCallback = diskStateListener::immediateUpdate;
    }

    protected abstract void setFilters(Set<ResourceKey> filters);

    protected abstract void setNormalizer(UnaryOperator<ResourceKey> normalizer);

    @Nullable
    public static Item getDisk(final CompoundTag tag, final int slot, final HolderLookup.Provider provider) {
        if (!tag.contains(TAG_DISK_INVENTORY)) {
            return null;
        }
        final CompoundTag diskInventoryTag = tag.getCompound(TAG_DISK_INVENTORY);
        if (!ContainerUtil.hasItemInSlot(diskInventoryTag, slot)) {
            return null;
        }
        final ItemStack diskStack = ContainerUtil.getItemInSlot(diskInventoryTag, slot, provider);
        return diskStack.isEmpty() ? null : diskStack.getItem();
    }

    void updateDiskStateIfNecessaryInLevel() {
        diskStateListener.updateIfNecessary();
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            initialize(level);
        }
    }

    /**
     * When loading a disk drive in a normal flow it is: #load(CompoundTag) -> #setLevel(Level).
     * Network initialization happens in #setLevel(Level).
     * Loading data before network initialization ensures that all nbt is present (and thus disks are available).
     * However, when we place a block entity with nbt, the flow is different:
     * #setLevel(Level) -> #load(CompoundTag) -> #setChanged().
     * #setLevel(Level) is called first (before #load(CompoundTag)) and initialization will happen BEFORE
     * we load the components!
     * That's why we need to override #setChanged() here, to ensure that the network and disks are still initialized
     * correctly in that case.
     */
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            initialize(level);
        }
    }

    private void initialize(final Level level) {
        diskInventory.setStorageRepository(PlatformApi.INSTANCE.getStorageRepository(level));
        mainNode.setProvider(diskInventory);
    }

    @Override
    public void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        diskStateListener.immediateUpdate();
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        fromClientTag(tag);
        if (tag.contains(TAG_DISK_INVENTORY)) {
            ContainerUtil.read(tag.getCompound(TAG_DISK_INVENTORY), diskInventory, provider);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filter.load(tag, provider);
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_DISK_INVENTORY, ContainerUtil.write(diskInventory, provider));
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        filter.save(tag, provider);
    }

    public FilteredContainer getDiskInventory() {
        return diskInventory;
    }

    private void onDiskChanged(final int slot) {
        // Level will not yet be present
        final boolean isJustPlacedIntoLevelOrLoading = level == null || level.isClientSide();
        // Level will be present, but network not yet
        final boolean isPlacedThroughDismantlingMode = mainNode.getNetwork() == null;
        if (isJustPlacedIntoLevelOrLoading || isPlacedThroughDismantlingMode) {
            return;
        }
        mainNode.onStorageChanged(slot);
        diskStateListener.immediateUpdate();
        setChanged();
    }

    private void fromClientTag(final CompoundTag tag) {
        if (!tag.contains(TAG_DISKS)) {
            return;
        }
        disks = diskInventory.fromSyncTag(tag.getList(TAG_DISKS, Tag.TAG_COMPOUND));
        onClientDriveStateUpdated();
    }

    protected void onClientDriveStateUpdated() {
        diskStateListener.immediateUpdate();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        // This null check is important. #getUpdateTag() can be called before the node's network is initialized!
        if (mainNode.getNetwork() == null) {
            return tag;
        }
        tag.put(TAG_DISKS, diskInventory.toSyncTag(mainNode::getState));
        return tag;
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        final NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            drops.add(diskInventory.getItem(i));
        }
        return drops;
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
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}
