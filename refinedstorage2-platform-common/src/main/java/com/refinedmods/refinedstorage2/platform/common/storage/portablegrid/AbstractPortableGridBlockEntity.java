package com.refinedmods.refinedstorage2.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridOperations;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
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
import com.refinedmods.refinedstorage2.platform.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.storage.Disk;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage2.platform.common.storage.DiskStateChangeListener;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.util.ContainerUtil;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPortableGridBlockEntity extends BlockEntity implements Grid, ExtendedMenuProvider,
    EnergyBlockEntity, ConfigurationCardTarget {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPortableGridBlockEntity.class);

    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "disks";
    private static final String TAG_STORED = "stored";
    private static final String TAG_REDSTONE_MODE = "rm";

    @Nullable
    protected Disk disk;

    private final DiskInventory diskInventory;
    private final DiskStateChangeListener diskStateListener = new DiskStateChangeListener(this);
    private final EnergyStorage energyStorage;
    private final RateLimiter activenessChangeRateLimiter = RateLimiter.create(1);

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    @Nullable
    private TypedStorage<?, StateTrackedStorage<?>> storage;

    protected AbstractPortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state);
        this.diskInventory = new DiskInventory(this::onDiskChanged, 1);
        this.energyStorage = createEnergyStorage(type, this);
    }

    private static EnergyStorage createEnergyStorage(final PortableGridType type, final BlockEntity blockEntity) {
        if (type == PortableGridType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            Platform.INSTANCE.getConfig().getController().getEnergyCapacity(), // TODO
            blockEntity
        );
    }

    void update(final BlockState state) {
        diskStateListener.updateIfNecessary();
        final boolean newActive = isGridActive();
        final boolean activenessNeedsUpdate = state.getValue(PortableGridBlock.ACTIVE) != newActive;
        if (activenessNeedsUpdate && activenessChangeRateLimiter.tryAcquire()) {
            updateActivenessBlockState(state, newActive);
        }
    }

    private void updateActivenessBlockState(final BlockState state, final boolean active) {
        if (level != null) {
            LOGGER.debug(
                "Sending block update at {} due to activeness change: {} -> {}",
                getBlockPos(),
                state.getValue(PortableGridBlock.ACTIVE),
                active
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(PortableGridBlock.ACTIVE, active));
        }
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
        if (tag.contains(TAG_STORED)) {
            energyStorage.receive(tag.getLong(TAG_STORED), Action.EXECUTE);
        }
        readConfiguration(tag);
        super.load(tag);
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        if (tag.contains(TAG_REDSTONE_MODE)) {
            redstoneMode = RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE));
        }
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
        tag.putLong(TAG_STORED, energyStorage.getStored());
        writeConfiguration(tag);
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(redstoneMode));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = new CompoundTag();
        tag.put(TAG_DISKS, diskInventory.toSyncTag(idx -> getStorageState()));
        return tag;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    private StorageState getStorageState() {
        if (storage == null) {
            return StorageState.NONE;
        }
        if (!isGridActive()) {
            return StorageState.INACTIVE;
        }
        return storage.storage().getState();
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
        return energyStorage.getStored() > 0
            && level != null
            && redstoneMode.isActive(level.hasNeighborSignal(worldPosition));
        // TODO: add energy component
        // TODO: sync activeness to block state
        // TODO: energy level in GUI
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

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        buf.writeLong(energyStorage.getStored());
        buf.writeLong(energyStorage.getCapacity());
    }

    public SimpleContainer getDiskInventory() {
        return diskInventory;
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    private static BlockEntityType<AbstractPortableGridBlockEntity> getBlockEntityType(final PortableGridType type) {
        return type == PortableGridType.CREATIVE
            ? BlockEntities.INSTANCE.getCreativePortableGrid()
            : BlockEntities.INSTANCE.getPortableGrid();
    }
}
