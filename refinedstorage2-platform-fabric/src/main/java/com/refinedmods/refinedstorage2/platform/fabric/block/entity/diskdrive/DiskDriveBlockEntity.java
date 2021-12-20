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
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.filter.ResourceFilterContainer;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.InternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.diskdrive.DiskDriveContainerMenu;

import java.util.Optional;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
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
import net.minecraft.world.MenuProvider;
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
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlockEntity extends InternalNetworkNodeContainerBlockEntity<DiskDriveNetworkNode> implements RenderAttachmentBlockEntity, MenuProvider, BlockEntityWithDrops, DiskDriveListener, ExtendedScreenHandlerFactory {
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
    private final ResourceFilterContainer resourceFilterContainer = new ResourceFilterContainer(9, this::resourceFilterContainerChanged);

    private DiskDriveState driveState;

    private boolean syncRequested;
    private long lastStateChanged;

    public DiskDriveBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getDiskDrive(), pos, state, new DiskDriveNetworkNode(
                Rs2Config.get().getDiskDrive().getEnergyUsage(),
                Rs2Config.get().getDiskDrive().getEnergyUsagePerDisk(),
                StorageChannelTypeRegistry.INSTANCE
        ));
        getNode().setDiskProvider(diskInventory);
        getNode().setListener(this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DiskDriveBlockEntity blockEntity) {
        InternalNetworkNodeContainerBlockEntity.serverTick(level, pos, state, blockEntity);
        blockEntity.updateDiskStateIfNecessaryInLevel();
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
        getNode().setFilterTemplates(resourceFilterContainer.getTemplates());
        setChanged();
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        if (!level.isClientSide()) {
            getNode().initialize(Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level));
        }
    }

    @Override
    public void activenessChanged(boolean active) {
        super.activenessChanged(active);
        sync();
    }

    @Override
    public void load(CompoundTag tag) {
        fromClientTag(tag);

        if (tag.contains(TAG_DISK_INVENTORY)) {
            diskInventory.fromTag(tag.getList(TAG_DISK_INVENTORY, Tag.TAG_COMPOUND));
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

        getNode().setFilterTemplates(resourceFilterContainer.getTemplates());

        super.load(tag);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_DISK_INVENTORY, diskInventory.createTag());
        tag.put(TAG_RESOURCE_FILTER, resourceFilterContainer.toTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(getNode().getFilterMode()));
        tag.putInt(TAG_PRIORITY, getNode().getPriority());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(getNode().getAccessMode()));
    }

    public SimpleContainer getDiskInventory() {
        return diskInventory;
    }

    public FilterMode getFilterMode() {
        return getNode().getFilterMode();
    }

    public void setFilterMode(FilterMode mode) {
        getNode().setFilterMode(mode);
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
        return getNode().getAccessMode();
    }

    public void setAccessMode(AccessMode accessMode) {
        getNode().setAccessMode(accessMode);
        setChanged();
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return driveState;
    }

    void onDiskChanged(int slot) {
        getNode().onDiskChanged(slot);
        sync();
        setChanged();
    }

    private void fromClientTag(CompoundTag tag) {
        if (tag.contains(TAG_STATES)) {
            ListTag statesList = tag.getList(TAG_STATES, Tag.TAG_BYTE);

            driveState = new DiskDriveState(statesList.size());
            for (int i = 0; i < statesList.size(); ++i) {
                int idx = ((ByteTag) statesList.get(i)).getAsInt();
                if (idx < 0 || idx >= StorageDiskState.values().length) {
                    idx = StorageDiskState.NONE.ordinal();
                }
                driveState.setState(i, StorageDiskState.values()[idx]);
            }

            // TODO: Still necessary?
            BlockState state = level.getBlockState(getBlockPos());
            level.sendBlockUpdated(getBlockPos(), state, state, 1 | 2);
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this, BlockEntity::getUpdateTag);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        ListTag statesList = new ListTag();
        for (StorageDiskState state : getNode().createState().getStates()) {
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
        return new DiskDriveContainerMenu(
                syncId,
                player,
                diskInventory,
                resourceFilterContainer,
                this,
                stack -> Optional.empty()
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

    public int getPriority() {
        return getNode().getPriority();
    }

    public void setPriority(int priority) {
        getNode().setPriority(priority);
        setChanged();
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
