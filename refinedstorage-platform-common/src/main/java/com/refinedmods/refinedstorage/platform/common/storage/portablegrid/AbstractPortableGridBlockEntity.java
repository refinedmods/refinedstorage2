package com.refinedmods.refinedstorage.platform.common.storage.portablegrid;

import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.support.energy.TransferableBlockEntityEnergy;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.platform.common.grid.GridData;
import com.refinedmods.refinedstorage.platform.common.grid.PortableGridData;
import com.refinedmods.refinedstorage.platform.common.storage.Disk;
import com.refinedmods.refinedstorage.platform.common.storage.DiskInventory;
import com.refinedmods.refinedstorage.platform.common.storage.DiskStateChangeListener;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneModeSettings;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.energy.BlockEntityEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.energy.CreativeEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.util.ContainerUtil;

import java.util.Optional;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPortableGridBlockEntity extends BlockEntity
    implements ExtendedMenuProvider<PortableGridData>, ConfigurationCardTarget, TransferableBlockEntityEnergy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPortableGridBlockEntity.class);

    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "disks";
    private static final String TAG_REDSTONE_MODE = "rm";

    @Nullable
    protected Disk disk;

    private final DiskInventory diskInventory;
    private final DiskStateChangeListener diskStateListener = new DiskStateChangeListener(this);
    private final EnergyStorage energyStorage;
    private final RateLimiter activenessChangeRateLimiter = RateLimiter.create(1);
    private final PortableGrid grid;

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;

    protected AbstractPortableGridBlockEntity(final PortableGridType type, final BlockPos pos, final BlockState state) {
        super(getBlockEntityType(type), pos, state);
        this.diskInventory = new DiskInventory((inventory, slot) -> onDiskChanged(), 1);
        this.energyStorage = createEnergyStorage(type, this);
        this.grid = new InWorldPortableGrid(energyStorage, diskInventory, diskStateListener, this);
    }

    static void readDiskInventory(final CompoundTag tag,
                                  final DiskInventory diskInventory,
                                  final HolderLookup.Provider provider) {
        if (tag.contains(TAG_DISK_INVENTORY)) {
            ContainerUtil.read(tag.getCompound(TAG_DISK_INVENTORY), diskInventory, provider);
        }
    }

    static void writeDiskInventory(final CompoundTag tag,
                                   final DiskInventory diskInventory,
                                   final HolderLookup.Provider provider) {
        tag.put(TAG_DISK_INVENTORY, ContainerUtil.write(diskInventory, provider));
    }

    static ItemStack getDisk(final CustomData customData, final HolderLookup.Provider provider) {
        final CompoundTag tag = customData.copyTag();
        if (!tag.contains(TAG_DISK_INVENTORY)) {
            return ItemStack.EMPTY;
        }
        return ContainerUtil.getItemInSlot(tag.getCompound(TAG_DISK_INVENTORY), 0, provider);
    }

    private static EnergyStorage createEnergyStorage(final PortableGridType type, final BlockEntity blockEntity) {
        if (type == PortableGridType.CREATIVE) {
            return CreativeEnergyStorage.INSTANCE;
        }
        return new BlockEntityEnergyStorage(
            new EnergyStorageImpl(Platform.INSTANCE.getConfig().getPortableGrid().getEnergyCapacity()),
            blockEntity
        );
    }

    Grid getGrid() {
        return grid;
    }

    void update(final BlockState state) {
        diskStateListener.updateIfNecessary();
        final boolean newActive = grid.isGridActive();
        final boolean activenessNeedsUpdate = state.getValue(PortableGridBlock.ACTIVE) != newActive;
        if (activenessNeedsUpdate && activenessChangeRateLimiter.tryAcquire()) {
            updateActivenessBlockState(state, newActive);
            grid.activeChanged(newActive);
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

    private void onDiskChanged() {
        final boolean isJustPlacedIntoLevelOrLoading = level == null || level.isClientSide();
        if (isJustPlacedIntoLevelOrLoading) {
            return;
        }
        grid.updateStorage();
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

    private void initialize(final Level level) {
        diskInventory.setStorageRepository(PlatformApi.INSTANCE.getStorageRepository(level));
        grid.updateStorage();
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        fromClientTag(tag);
        readDiskInventory(tag, diskInventory, provider);
        ItemBlockEnergyStorage.readFromTag(energyStorage, tag);
        readConfiguration(tag, provider);
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
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
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        writeDiskInventory(tag, diskInventory, provider);
        ItemBlockEnergyStorage.writeToTag(tag, energyStorage.getStored());
        writeConfiguration(tag, provider);
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(redstoneMode));
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        tag.put(TAG_DISKS, diskInventory.toSyncTag(idx -> grid.getStorageState()));
        return tag;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.PORTABLE_GRID;
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new PortableGridBlockContainerMenu(syncId, inventory, this);
    }

    @Override
    public PortableGridData getMenuData() {
        return new PortableGridData(
            GridData.of(grid),
            energyStorage.getStored(),
            energyStorage.getCapacity(),
            Optional.empty()
        );
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, PortableGridData> getMenuCodec() {
        return PortableGridData.STREAM_CODEC;
    }

    DiskInventory getDiskInventory() {
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
