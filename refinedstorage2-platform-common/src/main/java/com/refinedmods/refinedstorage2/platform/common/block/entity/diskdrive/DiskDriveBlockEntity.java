package com.refinedmods.refinedstorage2.platform.common.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.StorageSettingsProvider;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.EmptyStorageDiskInfoAccessor;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.util.ContainerUtil;
import com.refinedmods.refinedstorage2.platform.common.util.LevelUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class DiskDriveBlockEntity extends InternalNetworkNodeContainerBlockEntity<DiskDriveNetworkNode> implements BlockEntityWithDrops, DiskDriveListener, ExtendedMenuProvider, StorageSettingsProvider {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_EXACT_MODE = "em";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_STATES = "states";
    private static final String TAG_RESOURCE_FILTER = "rf";

    private static final int DISK_STATE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final DiskDriveInventory diskInventory = new DiskDriveInventory(this);
    private final ResourceFilterContainer resourceFilterContainer = new ResourceFilterContainer(PlatformApi.INSTANCE.getResourceTypeRegistry(), 9, this::resourceFilterContainerChanged);

    protected DiskDriveState driveState;

    private boolean syncRequested;
    private long lastStateChanged;

    private boolean exactMode;

    protected DiskDriveBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.INSTANCE.getDiskDrive(), pos, state, new DiskDriveNetworkNode(
                Platform.INSTANCE.getConfig().getDiskDrive().getEnergyUsage(),
                Platform.INSTANCE.getConfig().getDiskDrive().getEnergyUsagePerDisk(),
                StorageChannelTypeRegistry.INSTANCE
        ));
        getNode().setDiskProvider(diskInventory);
        getNode().setListener(this);
        getNode().setNormalizer(this::normalize);
    }

    public static void serverTick(Level level, BlockState state, DiskDriveBlockEntity blockEntity) {
        InternalNetworkNodeContainerBlockEntity.serverTick(level, state, blockEntity);
        blockEntity.updateDiskStateIfNecessaryInLevel();
    }

    public static boolean hasDisk(CompoundTag tag, int slot) {
        return tag.contains(TAG_DISK_INVENTORY) && ContainerUtil.hasItemInSlot(tag.getCompound(TAG_DISK_INVENTORY), slot);
    }

    private void updateDiskStateIfNecessaryInLevel() {
        if (!syncRequested) {
            return;
        }

        if (lastStateChanged == 0 || (System.currentTimeMillis() - lastStateChanged) > DISK_STATE_CHANGE_MINIMUM_INTERVAL_MS) {
            LOGGER.info("Disk state change for block at {}", getBlockPos());
            this.lastStateChanged = System.currentTimeMillis();
            this.syncRequested = false;
            sync();
        }
    }

    private void sync() {
        this.getLevel().sendBlockUpdated(worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }

    private void resourceFilterContainerChanged() {
        initializeResourceFilter();
        setChanged();
    }

    private void initializeResourceFilter() {
        getNode().setFilterTemplates(resourceFilterContainer.getTemplates());
    }

    private Object normalize(Object value) {
        if (exactMode) {
            return value;
        }
        if (value instanceof FuzzyModeNormalizer<?> fuzzyModeNormalizer) {
            return fuzzyModeNormalizer.normalize();
        }
        return value;
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            getNode().initialize(PlatformApi.INSTANCE.getStorageRepository(level));
        }
    }

    @Override
    public void activenessChanged(boolean active) {
        super.activenessChanged(active);
        LevelUtil.updateBlock(level, worldPosition, this.getBlockState());
    }

    /**
     * When loading a disk drive in a normal flow it is: #load(CompoundTag) -> #setLevel(Level).
     * Network initialization happens in #setLevel(Level).
     * Loading data before network initialization ensures that all nbt is present (and thus disks are available).
     * However, when we place a block entity with nbt, the flow is different: #setLevel(Level) -> #load(CompoundTag) -> #setChanged().
     * #setLevel(Level) is called first (before #load(CompoundTag)) and initialization will happen BEFORE we load the tag!
     * That's why we need to override #setChanged() here, to ensure that the network and disks are still initialized correctly in that case.
     */
    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide()) {
            getNode().initialize(PlatformApi.INSTANCE.getStorageRepository(level));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        fromClientTag(tag);

        if (tag.contains(TAG_DISK_INVENTORY)) {
            ContainerUtil.read(tag.getCompound(TAG_DISK_INVENTORY), diskInventory);
        }

        if (tag.contains(TAG_RESOURCE_FILTER)) {
            resourceFilterContainer.load(tag.getCompound(TAG_RESOURCE_FILTER));
        }

        if (tag.contains(TAG_PRIORITY)) {
            getNode().setPriority(tag.getInt(TAG_PRIORITY));
        }

        if (tag.contains(TAG_FILTER_MODE)) {
            getNode().setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
        }

        if (tag.contains(TAG_ACCESS_MODE)) {
            getNode().setAccessMode(AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE)));
        }

        if (tag.contains(TAG_EXACT_MODE)) {
            this.exactMode = tag.getBoolean(TAG_EXACT_MODE);
        }

        initializeResourceFilter();

        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_DISK_INVENTORY, ContainerUtil.write(diskInventory));
        tag.put(TAG_RESOURCE_FILTER, resourceFilterContainer.toTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getNode().getFilterMode()));
        tag.putInt(TAG_PRIORITY, getNode().getPriority());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(getNode().getAccessMode()));
        tag.putBoolean(TAG_EXACT_MODE, exactMode);
    }

    public SimpleContainer getDiskInventory() {
        return diskInventory;
    }

    @Override
    public FilterMode getFilterMode() {
        return getNode().getFilterMode();
    }

    @Override
    public void setFilterMode(FilterMode mode) {
        getNode().setFilterMode(mode);
        setChanged();
    }

    @Override
    public boolean isExactMode() {
        return exactMode;
    }

    @Override
    public void setExactMode(boolean exactMode) {
        this.exactMode = exactMode;
        initializeResourceFilter();
        setChanged();
    }

    @Override
    public AccessMode getAccessMode() {
        return getNode().getAccessMode();
    }

    @Override
    public void setAccessMode(AccessMode accessMode) {
        getNode().setAccessMode(accessMode);
        setChanged();
    }

    @Override
    public int getPriority() {
        return getNode().getPriority();
    }

    @Override
    public void setPriority(int priority) {
        getNode().setPriority(priority);
        setChanged();
    }

    void onDiskChanged(int slot) {
        getNode().onDiskChanged(slot);
        LevelUtil.updateBlock(level, worldPosition, this.getBlockState());
        setChanged();
    }

    @Override
    protected void onNetworkInNodeInitialized() {
        super.onNetworkInNodeInitialized();
        // It's important to sync here as the initial update packet might have failed as the network
        // could possibly be not initialized yet.
        LevelUtil.updateBlock(level, worldPosition, this.getBlockState());
    }

    private void fromClientTag(CompoundTag tag) {
        if (!tag.contains(TAG_STATES)) {
            return;
        }

        ListTag statesList = tag.getList(TAG_STATES, Tag.TAG_BYTE);

        driveState = new DiskDriveState(statesList.size());
        for (int i = 0; i < statesList.size(); ++i) {
            int idx = ((ByteTag) statesList.get(i)).getAsInt();
            if (idx < 0 || idx >= StorageDiskState.values().length) {
                idx = StorageDiskState.NONE.ordinal();
            }
            driveState.setState(i, StorageDiskState.values()[idx]);
        }

        onDriveStateUpdated();
    }

    protected void onDriveStateUpdated() {
        LevelUtil.updateBlock(level, worldPosition, this.getBlockState());
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        // This null check is important. #getUpdateTag() can be called before the node's network is initialized!
        if (getNode().getNetwork() == null) {
            return tag;
        }
        ListTag statesList = new ListTag();
        for (StorageDiskState state : getNode().createState().getStates()) {
            statesList.add(ByteTag.valueOf((byte) state.ordinal()));
        }
        tag.put(TAG_STATES, statesList);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "disk_drive");
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new DiskDriveContainerMenu(
                syncId,
                player,
                diskInventory,
                resourceFilterContainer,
                this,
                new EmptyStorageDiskInfoAccessor()
        );
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            drops.add(diskInventory.getItem(i));
        }
        return drops;
    }

    @Override
    public void onDiskChanged() {
        this.syncRequested = true;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        resourceFilterContainer.writeToUpdatePacket(buf);
    }
}
