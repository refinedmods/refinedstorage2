package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class AbstractDiskContainerBlockEntity<T extends AbstractStorageContainerNetworkNode>
    extends AbstractBaseNetworkNodeContainerBlockEntity<T>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData> {
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "disks";

    protected final FilterWithFuzzyMode filter;
    protected final DiskInventory diskInventory;
    protected Disk @Nullable [] disks;

    private final DiskStateChangeListener diskStateListener = new DiskStateChangeListener(this);

    protected AbstractDiskContainerBlockEntity(final BlockEntityType<?> type,
                                               final BlockPos pos,
                                               final BlockState state,
                                               final T node) {
        super(type, pos, state, node);
        this.diskInventory = new DiskInventory(inv -> onDiskChanged(), mainNetworkNode.getSize());
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueFilters(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            this::setFilters
        );
        this.mainNetworkNode.setListener(diskStateListener);
        setNormalizer(filter.createNormalizer());
    }

    @Override
    protected void containerInitialized() {
        super.containerInitialized();
        // It's important to sync here as the initial update packet might have failed as the network
        // could possibly be not initialized yet.
        PlatformUtil.sendBlockUpdateToClient(level, worldPosition);
    }

    protected abstract void setFilters(Set<ResourceKey> filters);

    protected abstract void setNormalizer(UnaryOperator<ResourceKey> normalizer);

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

    private void initialize(final Level level) {
        diskInventory.setStorageRepository(RefinedStorageApi.INSTANCE.getStorageRepository(level));
        mainNetworkNode.setProvider(diskInventory);
    }

    @Override
    public void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        PlatformUtil.sendBlockUpdateToClient(level, worldPosition);
    }

    @Override
    public void loadAdditional(final ValueInput input) {
        fromClientTag(input);
        input.read(TAG_DISK_INVENTORY, ItemContainerContents.CODEC)
            .ifPresent(contents -> contents.copyInto(diskInventory.getItems()));
        final boolean wasPlacedDismantled = level != null && !level.isClientSide();
        if (wasPlacedDismantled) {
            initialize(level);
        }
        super.loadAdditional(input);
    }

    @Override
    public void readConfiguration(final ValueInput input) {
        super.readConfiguration(input);
        filter.read(input);
    }

    @Override
    public void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store(TAG_DISK_INVENTORY, ItemContainerContents.CODEC,
            ItemContainerContents.fromItems(diskInventory.getItems()));
    }

    @Override
    public void writeConfiguration(final ValueOutput output) {
        super.writeConfiguration(output);
        filter.store(output);
    }

    public FilteredContainer getDiskInventory() {
        return diskInventory;
    }

    private void onDiskChanged() {
        mainNetworkNode.onStorageChanged();
        PlatformUtil.sendBlockUpdateToClient(level, worldPosition);
        setChanged();
    }

    private void fromClientTag(final ValueInput input) {
        input.read(TAG_DISKS, Disk.LIST_CODEC).ifPresent(d -> {
            disks = d.toArray(new Disk[0]);
            onClientDriveStateUpdated();
        });
    }

    protected void onClientDriveStateUpdated() {
        Platform.INSTANCE.requestModelDataUpdateOnClient(this, true);
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider registries) {
        final CompoundTag tag = super.getUpdateTag(registries);
        // This null check is important. #getUpdateTag() can be called before the node's network is initialized!
        if (mainNetworkNode.getNetwork() == null) {
            return tag;
        }
        final List<Disk> diskState = new ArrayList<>();
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            final ItemStack diskItem = diskInventory.getItem(i);
            diskState.add(new Disk(diskItem.isEmpty() ? null : diskItem.getItem(), mainNetworkNode.getState(i)));
        }
        tag.store(TAG_DISKS, Disk.LIST_CODEC, diskState);
        return tag;
    }

    @Override
    public void preRemoveSideEffects(final BlockPos pos, final BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropContents(level, pos, diskInventory.getItems());
        }
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
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    public static List<@Nullable Item> getDisks(final CompoundTag tag, final int size) {
        final List<@Nullable Item> disks = new ArrayList<>(size);
        tag.read(TAG_DISK_INVENTORY, ItemContainerContents.CODEC).ifPresent(contents -> {
            final NonNullList<ItemStack> inventoryContents = NonNullList.withSize(size, ItemStack.EMPTY);
            contents.copyInto(inventoryContents);
            for (int i = 0; i < size; ++i) {
                disks.add(inventoryContents.get(i).isEmpty() ? null : inventoryContents.get(i).getItem());
            }
        });
        return disks;
    }
}
