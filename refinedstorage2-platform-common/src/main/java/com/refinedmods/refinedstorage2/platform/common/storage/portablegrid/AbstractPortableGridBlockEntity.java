package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.api.storage.StateTrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.StorageState;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskStateChangeListener;
import com.refinedmods.refinedstorage2.platform.common.util.ContainerUtil;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractPortableGridBlockEntity extends BlockEntity implements Grid, MenuProvider {
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "disks";

    @Nullable
    protected Disk disk;

    private final DiskInventory diskInventory;
    private final DiskStateChangeListener diskStateListener = new DiskStateChangeListener(this);
    @Nullable
    private TypedStorage<?, StateTrackedStorage<?>> storage;

    protected AbstractPortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state);
        this.diskInventory = new DiskInventory(this::onDiskChanged, 1);
    }

    void updateDiskStateIfNecessaryInLevel() {
        diskStateListener.updateIfNecessary();
    }

    private void onDiskChanged(final int slot) {
        final boolean isJustPlacedIntoLevelOrLoading = level == null || level.isClientSide();
        if (isJustPlacedIntoLevelOrLoading) {
            return;
        }
        updateStorage();
        diskStateListener.immediateUpdate();
        setChanged();
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            initialize(level);
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            initialize(level);
        }
    }

    private void initialize(final Level level) {
        diskInventory.setStorageRepository(PlatformApi.INSTANCE.getStorageRepository(level));
        updateStorage();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void updateStorage() {
        this.storage = diskInventory.resolve(0)
            .map(resolved -> (TypedStorage) StateTrackedStorage.of(resolved, diskStateListener))
            .orElse(null);
    }

    @Override
    public void load(final CompoundTag tag) {
        fromClientTag(tag);
        if (tag.contains(TAG_DISK_INVENTORY)) {
            ContainerUtil.read(tag.getCompound(TAG_DISK_INVENTORY), diskInventory);
        }
        super.load(tag);
    }

    private void fromClientTag(final CompoundTag tag) {
        if (!tag.contains(TAG_DISKS)) {
            return;
        }
        disk = diskInventory.fromSyncTag(tag.getList(TAG_DISKS, Tag.TAG_COMPOUND))[0];
        onClientDriveStateUpdated();
    }

    protected void onClientDriveStateUpdated() {
        diskStateListener.immediateUpdate();
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_DISK_INVENTORY, ContainerUtil.write(diskInventory));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = new CompoundTag();
        tag.put(TAG_DISKS, diskInventory.toSyncTag(idx -> getState()));
        return tag;
    }

    private StorageState getState() {
        return storage != null ? storage.storage().getState() : StorageState.NONE;
    }

    @Override
    public void addWatcher(final GridWatcher watcher, final Class<? extends Actor> actorType) {
        // TODO
    }

    @Override
    public void removeWatcher(final GridWatcher watcher) {
        // TODO
    }

    @Override
    public Storage<ItemResource> getItemStorage() {
        return new InMemoryStorageImpl<>();
    }

    @Override
    public boolean isGridActive() {
        // TODO: add energy component
        // TODO: sync activeness to block state
        // TODO: energy level in GUI
        return true;
    }

    @Override
    public <T> List<TrackedResourceAmount<T>> getResources(final StorageChannelType<T> type,
                                                           final Class<? extends Actor> actorType) {
        // TODO
        return Collections.emptyList();
    }

    @Override
    public <T> GridOperations<T> createOperations(final PlatformStorageChannelType<T> storageChannelType,
                                                  final Actor actor) {
        // TODO
        return new GridOperations<>() {
            @Override
            public boolean extract(final T resource, final GridExtractMode extractMode,
                                   final InsertableStorage<T> destination) {
                return false;
            }

            @Override
            public boolean insert(final T resource, final GridInsertMode insertMode,
                                  final ExtractableStorage<T> source) {
                return false;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.PORTABLE_GRID;
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PortableGridContainerMenu(syncId, inventory, this);
    }

    public SimpleContainer getDiskInventory() {
        return diskInventory;
    }

    private static BlockEntityType<AbstractPortableGridBlockEntity> getBlockEntityType(final PortableGridType type) {
        return type == PortableGridType.CREATIVE
            ? BlockEntities.INSTANCE.getCreativePortableGrid()
            : BlockEntities.INSTANCE.getPortableGrid();
    }
}
