package com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveListener;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.api.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.api.stack.filter.FilterMode;
import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.api.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.ItemStacks;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;

import java.util.List;
import java.util.Optional;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlockEntity extends FabricNetworkNodeContainerBlockEntity<DiskDriveNetworkNode> implements RenderAttachmentBlockEntity, BlockEntityClientSerializable, NamedScreenHandlerFactory, BlockEntityWithDrops, DiskDriveListener {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_EXACT_MODE = "em";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_FILTER_INVENTORY = "fi";
    private static final String TAG_STATES = "states";

    private static final int DISK_STATE_CHANGE_MINIMUM_INTERVAL_MS = 1000;

    private final DiskDriveInventory diskInventory = new DiskDriveInventory();
    private final FullFixedItemInv filterInventory = new FullFixedItemInv(9);
    private DiskDriveState driveState;

    private boolean syncRequested;
    private long lastStateChanged;

    public DiskDriveBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getDiskDrive(), pos, state);

        diskInventory.setOwnerListener(new DiskInventoryListener(this));
        filterInventory.setOwnerListener(new FilterInventoryListener(this));
    }

    public void updateDiskStateIfNecessary() {
        if (!syncRequested) {
            return;
        }

        if (lastStateChanged == 0 || (System.currentTimeMillis() - lastStateChanged) > DISK_STATE_CHANGE_MINIMUM_INTERVAL_MS) {
            LOGGER.info("Disk state change for block at {}", pos);
            this.lastStateChanged = System.currentTimeMillis();
            this.syncRequested = false;
            sync();
        }
    }

    @Override
    public void setWorld(World world) {
        super.setWorld(world);
        if (!world.isClient()) {
            getContainer().getNode().initialize(Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world));
        }
    }

    @Override
    public void activenessChanged(boolean active) {
        super.activenessChanged(active);
        sync();
    }

    @Override
    protected DiskDriveNetworkNode createNode(BlockPos pos, NbtCompound tag) {
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

            if (tag.contains(TAG_EXACT_MODE)) {
                diskDrive.setExactMode(tag.getBoolean(TAG_EXACT_MODE));
            }

            if (tag.contains(TAG_ACCESS_MODE)) {
                diskDrive.setAccessMode(AccessModeSettings.getAccessMode(tag.getInt(TAG_ACCESS_MODE)));
            }
        }

        return diskDrive;
    }

    @Override
    public void readNbt(NbtCompound tag) {
        super.readNbt(tag);

        if (tag.contains(TAG_DISK_INVENTORY)) {
            diskInventory.fromTag(tag.getCompound(TAG_DISK_INVENTORY));
        }

        if (tag.contains(TAG_FILTER_INVENTORY)) {
            filterInventory.fromTag(tag.getCompound(TAG_FILTER_INVENTORY));
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag = super.writeNbt(tag);
        tag.put(TAG_DISK_INVENTORY, diskInventory.toTag());
        tag.put(TAG_FILTER_INVENTORY, filterInventory.toTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getContainer().getNode().getFilterMode()));
        tag.putInt(TAG_PRIORITY, getContainer().getNode().getPriority());
        tag.putBoolean(TAG_EXACT_MODE, getContainer().getNode().isExactMode());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(getContainer().getNode().getAccessMode()));
        return tag;
    }

    public FixedItemInv getDiskInventory() {
        return diskInventory;
    }

    public FilterMode getFilterMode() {
        return getContainer().getNode().getFilterMode();
    }

    public void setFilterMode(FilterMode mode) {
        getContainer().getNode().setFilterMode(mode);
        markDirty();
    }

    public boolean isExactMode() {
        return getContainer().getNode().isExactMode();
    }

    public void setExactMode(boolean exactMode) {
        getContainer().getNode().setExactMode(exactMode);
        markDirty();
    }

    public AccessMode getAccessMode() {
        return getContainer().getNode().getAccessMode();
    }

    public void setAccessMode(AccessMode accessMode) {
        getContainer().getNode().setAccessMode(accessMode);
        markDirty();
    }

    public void setFilterTemplates(List<ItemStack> templates) {
        getContainer().getNode().setFilterTemplates(templates.stream().map(ItemStacks::ofItemStack).toList());
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return driveState;
    }

    void onDiskChanged(int slot) {
        getContainer().getNode().onDiskChanged(slot);
        sync();
        markDirty();
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        if (tag.contains(TAG_STATES)) {
            NbtList statesList = tag.getList(TAG_STATES, NbtType.BYTE);

            driveState = new DiskDriveState(statesList.size());

            for (int i = 0; i < statesList.size(); ++i) {
                int idx = ((NbtByte) statesList.get(i)).intValue();
                if (idx < 0 || idx >= DiskState.values().length) {
                    idx = DiskState.NONE.ordinal();
                }
                driveState.setState(i, DiskState.values()[idx]);
            }

            BlockState state = world.getBlockState(pos);
            world.updateListeners(pos, state, state, 1 | 2);
        }
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        NbtList statesList = new NbtList();
        for (DiskState state : getContainer().getNode().createState().getStates()) {
            statesList.add(NbtByte.of((byte) state.ordinal()));
        }
        tag.put(TAG_STATES, statesList);
        return tag;
    }

    @Override
    public Text getDisplayName() {
        return Rs2Mod.createTranslation("block", "disk_drive");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new DiskDriveScreenHandler(syncId, player, diskInventory, filterInventory, this, stack -> Optional.empty());
    }

    @Override
    public DefaultedList<ItemStack> getDrops() {
        DefaultedList<ItemStack> drops = DefaultedList.of();
        diskInventory.stackIterable().forEach(drops::add);
        return drops;
    }

    public int getPriority() {
        return getContainer().getNode().getPriority();
    }

    public void setPriority(int priority) {
        getContainer().getNode().setPriority(priority);
        markDirty();
    }

    @Override
    public void onDiskChanged() {
        this.syncRequested = true;
    }
}
