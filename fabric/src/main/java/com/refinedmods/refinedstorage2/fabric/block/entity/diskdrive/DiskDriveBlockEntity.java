package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.FilterMode;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.ItemStacks;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlockEntity extends NetworkNodeBlockEntity<DiskDriveNetworkNode> implements Storage<Rs2ItemStack>, RenderAttachmentBlockEntity, BlockEntityClientSerializable, NamedScreenHandlerFactory, BlockEntityWithDrops {
    private static final String TAG_PRIORITY = "pri";
    private static final String TAG_FILTER_MODE = "fim";
    private static final String TAG_EXACT_MODE = "em";
    private static final String TAG_ACCESS_MODE = "am";
    private static final String TAG_DISK_INVENTORY = "inv";
    private static final String TAG_FILTER_INVENTORY = "fi";
    private static final String TAG_STATES = "states";

    private final DiskDriveInventory diskInventory = new DiskDriveInventory();
    private final FullFixedItemInv filterInventory = new FullFixedItemInv(9);
    private DiskDriveState driveState;

    public DiskDriveBlockEntity() {
        super(Rs2Mod.BLOCK_ENTITIES.getDiskDrive());

        diskInventory.setOwnerListener(new DiskInventoryListener(this));
        filterInventory.setOwnerListener(new FilterInventoryListener(this));
    }

    @Override
    public void onActiveChanged(boolean active) {
        super.onActiveChanged(active);
        sync();
    }

    @Override
    protected DiskDriveNetworkNode createNode(World world, BlockPos pos, CompoundTag tag) {
        DiskDriveNetworkNode diskDrive = new DiskDriveNetworkNode(
                FabricRs2WorldAdapter.of(world),
                Positions.ofBlockPos(pos),
                FabricNetworkNodeReference.of(world, pos),
                Rs2Mod.API.getStorageDiskManager(world),
                diskInventory,
                Rs2Config.get().getDiskDrive().getEnergyUsage(),
                Rs2Config.get().getDiskDrive().getEnergyUsagePerDisk()
        );

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

        for (int slot = 0; slot < diskInventory.getSlotCount(); ++slot) {
            diskDrive.onDiskChanged(slot);
        }

        return diskDrive;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        if (tag.contains(TAG_DISK_INVENTORY)) {
            diskInventory.fromTag(tag.getCompound(TAG_DISK_INVENTORY));
        }

        if (tag.contains(TAG_FILTER_INVENTORY)) {
            filterInventory.fromTag(tag.getCompound(TAG_FILTER_INVENTORY));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag = super.toTag(tag);
        tag.put(TAG_DISK_INVENTORY, diskInventory.toTag());
        tag.put(TAG_FILTER_INVENTORY, filterInventory.toTag());
        tag.putInt(TAG_FILTER_MODE, FilterModeSettings.getFilterMode(node.getFilterMode()));
        tag.putInt(TAG_PRIORITY, node.getPriority());
        tag.putBoolean(TAG_EXACT_MODE, node.isExactMode());
        tag.putInt(TAG_ACCESS_MODE, AccessModeSettings.getAccessMode(node.getAccessMode()));
        return tag;
    }

    public FixedItemInv getDiskInventory() {
        return diskInventory;
    }

    public FilterMode getFilterMode() {
        return node.getFilterMode();
    }

    public void setFilterMode(FilterMode mode) {
        node.setFilterMode(mode);
        markDirty();
    }

    public boolean isExactMode() {
        return node.isExactMode();
    }

    public void setExactMode(boolean exactMode) {
        node.setExactMode(exactMode);
        markDirty();
    }

    public AccessMode getAccessMode() {
        return node.getAccessMode();
    }

    public void setAccessMode(AccessMode accessMode) {
        node.setAccessMode(accessMode);
        markDirty();
    }

    public void setFilterTemplates(List<ItemStack> templates) {
        node.setFilterTemplates(templates.stream().map(ItemStacks::ofItemStack).collect(Collectors.toList()));
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return driveState;
    }

    void onDiskChanged(int slot) {
        node.onDiskChanged(slot);
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        if (tag.contains(TAG_STATES)) {
            ListTag statesList = tag.getList(TAG_STATES, NbtType.BYTE);

            driveState = new DiskDriveState(statesList.size());

            for (int i = 0; i < statesList.size(); ++i) {
                int idx = ((ByteTag) statesList.get(i)).getInt();
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
    public CompoundTag toClientTag(CompoundTag tag) {
        ListTag statesList = new ListTag();
        for (DiskState state : node.createState().getStates()) {
            statesList.add(ByteTag.of((byte) state.ordinal()));
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

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        return node.extract(template, amount, action);
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
        return node.insert(template, amount, action);
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
        return node.getStacks();
    }

    @Override
    public long getStored() {
        return node.getStored();
    }

    public int getPriority() {
        return node.getPriority();
    }

    public void setPriority(int priority) {
        node.setPriority(priority);
        node.getNetwork().getItemStorageChannel().sortSources();
        markDirty();
    }
}
