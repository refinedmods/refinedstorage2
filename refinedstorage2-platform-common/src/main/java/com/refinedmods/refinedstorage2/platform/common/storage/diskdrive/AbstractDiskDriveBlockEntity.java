package com.refinedmods.refinedstorage2.platform.common.storage.diskdrive;

import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageListener;
import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageNetworkNode;
import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageStorageState;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.storage.StorageConfigurationContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage2.platform.common.util.ContainerUtil;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDiskDriveBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<MultiStorageNetworkNode>
    implements BlockEntityWithDrops, MultiStorageListener, ExtendedMenuProvider {
    public static final int AMOUNT_OF_DISKS = 8;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDiskDriveBlockEntity.class);

    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_DISKS = "states";
    private static final String TAG_DISK_STATE = "s";
    private static final String TAG_DISK_ITEM_ID = "i";

    @Nullable
    protected DiskDriveDisk[] disks;

    private final DiskDriveInventory diskInventory;
    private final FilterWithFuzzyMode filter;
    private final StorageConfigurationContainerImpl configContainer;
    private final RateLimiter diskStateChangeRateLimiter = RateLimiter.create(1);

    private boolean syncRequested;

    protected AbstractDiskDriveBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getDiskDrive(), pos, state, new MultiStorageNetworkNode(
            Platform.INSTANCE.getConfig().getDiskDrive().getEnergyUsage(),
            Platform.INSTANCE.getConfig().getDiskDrive().getEnergyUsagePerDisk(),
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry().getAll(),
            AMOUNT_OF_DISKS
        ));
        this.diskInventory = new DiskDriveInventory(this, getNode().getSize());
        this.filter = FilterWithFuzzyMode.createAndListenForUniqueTemplates(
            ResourceContainerImpl.createForFilter(),
            this::setChanged,
            templates -> getNode().setFilterTemplates(templates)
        );
        this.configContainer = new StorageConfigurationContainerImpl(
            getNode(),
            filter,
            this::setChanged,
            this::getRedstoneMode,
            this::setRedstoneMode
        );
        getNode().setListener(this);
        getNode().setNormalizer(filter.createNormalizer());
    }

    public static boolean hasDisk(final CompoundTag tag, final int slot) {
        return tag.contains(TAG_DISK_INVENTORY)
            && ContainerUtil.hasItemInSlot(tag.getCompound(TAG_DISK_INVENTORY), slot);
    }

    void updateDiskStateIfNecessaryInLevel() {
        if (!syncRequested) {
            return;
        }
        if (diskStateChangeRateLimiter.tryAcquire()) {
            LOGGER.debug("Disk state change for block at {}", getBlockPos());
            this.syncRequested = false;
            sync();
        }
    }

    private void sync() {
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
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
     * we load the tag!
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
        getNode().setProvider(diskInventory);
    }

    @Override
    public void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        updateBlock();
    }

    @Override
    public void load(final CompoundTag tag) {
        fromClientTag(tag);
        if (tag.contains(TAG_DISK_INVENTORY)) {
            ContainerUtil.read(tag.getCompound(TAG_DISK_INVENTORY), diskInventory);
        }
        super.load(tag);
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        configContainer.load(tag);
        filter.load(tag);
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_DISK_INVENTORY, ContainerUtil.write(diskInventory));
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        configContainer.save(tag);
        filter.save(tag);
    }

    public SimpleContainer getDiskInventory() {
        return diskInventory;
    }

    void onDiskChanged(final int slot) {
        getNode().onStorageChanged(slot);
        updateBlock();
        setChanged();
    }

    @Override
    public void onStorageChanged() {
        this.syncRequested = true;
    }

    @Override
    protected void onNetworkInNodeInitialized() {
        super.onNetworkInNodeInitialized();
        // It's important to sync here as the initial update packet might have failed as the network
        // could possibly be not initialized yet.
        updateBlock();
    }

    private void fromClientTag(final CompoundTag tag) {
        if (!tag.contains(TAG_DISKS)) {
            return;
        }
        final ListTag disksList = tag.getList(TAG_DISKS, Tag.TAG_COMPOUND);
        disks = new DiskDriveDisk[disksList.size()];
        for (int i = 0; i < disksList.size(); ++i) {
            final CompoundTag diskTag = disksList.getCompound(i);
            disks[i] = BuiltInRegistries.ITEM.getHolder(diskTag.getInt(TAG_DISK_ITEM_ID))
                .map(item -> new DiskDriveDisk(item.value(), getState(diskTag)))
                .orElse(new DiskDriveDisk(null, MultiStorageStorageState.NONE));
        }
        onDriveStateUpdated();
    }

    private MultiStorageStorageState getState(final CompoundTag tag) {
        final int stateOrdinal = tag.getByte(TAG_DISK_STATE);
        final MultiStorageStorageState[] values = MultiStorageStorageState.values();
        if (stateOrdinal < 0 || stateOrdinal >= values.length) {
            return MultiStorageStorageState.NONE;
        }
        return values[stateOrdinal];
    }

    protected void onDriveStateUpdated() {
        updateBlock();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = new CompoundTag();
        // This null check is important. #getUpdateTag() can be called before the node's network is initialized!
        if (getNode().getNetwork() == null) {
            return tag;
        }
        final ListTag disksList = new ListTag();
        for (int i = 0; i < getNode().getSize(); ++i) {
            final CompoundTag disk = new CompoundTag();
            disk.putByte(TAG_DISK_STATE, (byte) getNode().getState(i).ordinal());
            final ItemStack diskItem = diskInventory.getItem(i);
            if (!diskItem.isEmpty()) {
                disk.putInt(TAG_DISK_ITEM_ID, BuiltInRegistries.ITEM.getId(diskItem.getItem()));
            }
            disksList.add(disk);
        }
        tag.put(TAG_DISKS, disksList);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.DISK_DRIVE;
    }

    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inv, final Player player) {
        return new DiskDriveContainerMenu(
            syncId,
            player,
            diskInventory,
            filter.getFilterContainer(),
            configContainer,
            new EmptyStorageDiskInfoAccessor()
        );
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
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState);
    }
}
