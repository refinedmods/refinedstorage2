package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.impl.FullFixedItemInv;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveNetworkNode;
import com.refinedmods.refinedstorage2.core.network.node.diskdrive.DiskDriveState;
import com.refinedmods.refinedstorage2.core.storage.AccessMode;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.core.util.FilterMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.AccessModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.fabric.block.entity.FilterModeSettings;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricWorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;
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

public class DiskDriveBlockEntity extends NetworkNodeBlockEntity<DiskDriveNetworkNode> implements Storage<ItemStack>, RenderAttachmentBlockEntity, BlockEntityClientSerializable, NamedScreenHandlerFactory, BlockEntityWithDrops {
    private final DiskDriveInventory diskInventory = new DiskDriveInventory();
    private final FullFixedItemInv filterInventory = new FullFixedItemInv(9);
    private DiskDriveState driveState;

    public DiskDriveBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getDiskDrive());

        diskInventory.setOwnerListener(new DiskInventoryListener(this));
        filterInventory.setOwnerListener(new FilterInventoryListener(this));
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);

        if (!world.isClient()) {
            for (int i = 0; i < diskInventory.getSlotCount(); ++i) {
                onDiskChanged(i);
            }
        }
    }

    @Override
    protected DiskDriveNetworkNode createNode(World world, BlockPos pos) {
        return new DiskDriveNetworkNode(
                FabricWorldAdapter.of(world),
                pos,
                FabricNetworkNodeReference.of(world, pos),
                RefinedStorage2Mod.API.getStorageDiskManager(world),
                diskInventory
        );
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        if (tag.contains("inv")) {
            diskInventory.fromTag(tag.getCompound("inv"));
        }

        if (tag.contains("fi")) {
            filterInventory.fromTag(tag.getCompound("fi"));
        }

        if (tag.contains("pri")) {
            node.setPriority(tag.getInt("pri"));
        }

        if (tag.contains("fim")) {
            node.setFilterMode(FilterModeSettings.getFilterMode(tag.getInt("fim")));
        }

        if (tag.contains("em")) {
            node.setExactMode(tag.getBoolean("em"));
        }

        if (tag.contains("am")) {
            node.setAccessMode(AccessModeSettings.getAccessMode(tag.getInt("am")));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag = super.toTag(tag);
        tag.put("inv", diskInventory.toTag());
        tag.put("fi", filterInventory.toTag());
        tag.putInt("fim", FilterModeSettings.getFilterMode(node.getFilterMode()));
        tag.putInt("pri", node.getPriority());
        tag.putBoolean("em", node.isExactMode());
        tag.putInt("am", AccessModeSettings.getAccessMode(node.getAccessMode()));
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
        node.setFilterTemplates(templates);
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
        if (tag.contains("states")) {
            ListTag statesList = tag.getList("states", NbtType.BYTE);

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
        tag.put("states", statesList);
        return tag;
    }

    @Override
    public Text getDisplayName() {
        return RefinedStorage2Mod.createTranslation("block", "disk_drive");
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
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        return node.extract(template, amount, action);
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        return node.insert(template, amount, action);
    }

    @Override
    public Collection<ItemStack> getStacks() {
        return node.getStacks();
    }

    @Override
    public int getStored() {
        return node.getStored();
    }

    public int getPriority() {
        return node.getPriority();
    }

    public void setPriority(int priority) {
        node.setPriority(priority);
        markDirty();
    }
}
