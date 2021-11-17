package com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.StorageDiskState;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive.DiskDriveContainerMenu;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlockEntity extends FabricNetworkNodeContainerBlockEntity<DiskDriveNetworkNode> implements RenderAttachmentBlockEntity, BlockEntityClientSerializable, MenuProvider, BlockEntityWithDrops, DiskDriveListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_EXACT_MODE = "em";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_FILTER_INVENTORY = "fi";
    private static final String TAG_STATES = "states";

    private static final int DISK_STATE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final DiskDriveInventory diskInventory = new DiskDriveInventory(this);
    private final SimpleContainer filterInventory = new SimpleContainer(9);
    private DiskDriveState driveState;

    private boolean syncRequested;
    private long lastStateChanged;

    public DiskDriveBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getDiskDrive(), pos, state);
        filterInventory.addListener(new FilterInventoryChangedListener(this));
    }

    public void updateDiskStateIfNecessary() {
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

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            getContainer().getNode().initialize(Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level));
        }
    }

    @Override
    public void activenessChanged(boolean active) {
        super.activenessChanged(active);
        sync();
    }

    @Override
    protected DiskDriveNetworkNode createNode(BlockPos pos, CompoundTag tag) {
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(
                diskInventory,
                Rs2Config.get().getDiskDrive().getEnergyUsage(),
                Rs2Config.get().getDiskDrive().getEnergyUsagePerDisk(),
                this,
                StorageChannelTypeRegistry.INSTANCE
        );

        if (tag != null) {
            if (tag.contains(TAG_PRIORITY)) {
                diskDrive.setPriority(tag.getInt(TAG_PRIORITY));
            }

            if (tag.contains(TAG_FILTER_MODE)) {
                diskDrive.setFilterMode(FilterModeSettings.getFilterMode(tag.getInt(TAG_FILTER_MODE)));
            }

            if (tag.contains(TAG_ACCESS_MODE)) {
                diskDrive.setAccessMode(AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE)));
            }
        }

        Set<Object> filterTemplates = new HashSet<>();
        for (int i = 0; i < filterInventory.getContainerSize(); ++i) {
            ItemStack filter = filterInventory.getItem(i);
            if (!filter.isEmpty()) {
                filterTemplates.add(new ItemResource(filter));
            }
        }

        diskDrive.setFilterTemplates(filterTemplates);

        return diskDrive;
    }

    @Override
    public void load(CompoundTag tag) {
        if (tag.contains(TAG_DISK_INVENTORY)) {
            diskInventory.fromTag(tag.getList(TAG_DISK_INVENTORY, ByteTag.TAG_COMPOUND));
        }

        if (tag.contains(TAG_FILTER_INVENTORY)) {
            filterInventory.fromTag(tag.getList(TAG_FILTER_INVENTORY, ByteTag.TAG_COMPOUND));
        }

        super.load(tag);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag = super.save(tag);
        tag.put(TAG_DISK_INVENTORY, diskInventory.createTag());
        tag.put(TAG_FILTER_INVENTORY, filterInventory.createTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getContainer().getNode().getFilterMode()));
        tag.putInt(TAG_PRIORITY, getContainer().getNode().getPriority());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(getContainer().getNode().getAccessMode()));
        return tag;
    }

    public SimpleContainer getDiskInventory() {
        return diskInventory;
    }

    public FilterMode getFilterMode() {
        return getContainer().getNode().getFilterMode();
    }

    public void setFilterMode(FilterMode mode) {
        getContainer().getNode().setFilterMode(mode);
        setChanged();
    }

    public boolean isExactMode() {
        // todo
        return false;
    }

    public void setExactMode(boolean exactMode) {
        // todo
        setChanged();
    }

    public AccessMode getAccessMode() {
        return getContainer().getNode().getAccessMode();
    }

    public void setAccessMode(AccessMode accessMode) {
        getContainer().getNode().setAccessMode(accessMode);
        setChanged();
    }

    public void setFilterTemplates(List<ItemStack> templates) {
        getContainer().getNode().setFilterTemplates(templates.stream().map(ItemResource::new).collect(Collectors.toSet()));
        setChanged();
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return driveState;
    }

    void onDiskChanged(int slot) {
        getContainer().getNode().onDiskChanged(slot);
        sync();
        setChanged();
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        if (tag.contains(TAG_STATES)) {
            ListTag statesList = tag.getList(TAG_STATES, NbtType.BYTE);

            driveState = new DiskDriveState(statesList.size());

            for (int i = 0; i < statesList.size(); ++i) {
                int idx = ((ByteTag) statesList.get(i)).getAsInt();
                if (idx < 0 || idx >= StorageDiskState.values().length) {
                    idx = StorageDiskState.NONE.ordinal();
                }
                driveState.setState(i, StorageDiskState.values()[idx]);
            }

            BlockState state = level.getBlockState(getBlockPos());
            level.sendBlockUpdated(getBlockPos(), state, state, 1 | 2);
        }
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        ListTag statesList = new ListTag();
        for (StorageDiskState state : getContainer().getNode().createState().getStates()) {
            statesList.add(ByteTag.valueOf((byte) state.ordinal()));
        }
        tag.put(TAG_STATES, statesList);
        return tag;
    }

    @Override
    public Component getDisplayName() {
        return Rs2Mod.createTranslation("block", "disk_drive");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new DiskDriveContainerMenu(syncId, player, diskInventory, filterInventory, this, stack -> Optional.empty());
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        NonNullList<ItemStack> drops = NonNullList.create();
        for (int i = 0; i < diskInventory.getContainerSize(); ++i) {
            drops.add(diskInventory.getItem(i));
        }
        return drops;
    }

    public int getPriority() {
        return getContainer().getNode().getPriority();
    }

    public void setPriority(int priority) {
        getContainer().getNode().setPriority(priority);
        setChanged();
    }

    @Override
    public void onDiskChanged() {
        this.syncRequested = true;
    }
}
