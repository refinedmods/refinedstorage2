package com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive;

import alexiil.mc.lib.attributes.item.FixedItemInv;
import alexiil.mc.lib.attributes.item.FixedItemInvView;
import alexiil.mc.lib.attributes.item.ItemInvSlotChangeListener;
import com.refinedmods.refinedstorage2.core.storage.disk.DiskState;
import com.refinedmods.refinedstorage2.core.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.item.StorageDiskItem;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DiskDriveBlockEntity extends NetworkNodeBlockEntity implements RenderAttachmentBlockEntity, ItemInvSlotChangeListener, BlockEntityClientSerializable {
    private static final int DISK_COUNT = 8;

    private final DiskDriveInventory diskInventory = new DiskDriveInventory();
    private final StorageDisk[] storageDisks = new StorageDisk[DISK_COUNT];
    private final List<DiskState> diskStates = new ArrayList<>(DISK_COUNT);

    public DiskDriveBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getDiskDrive());

        diskInventory.setOwnerListener(this);

        for (int i = 0; i < DISK_COUNT; ++i) {
            diskStates.add(DiskState.NONE);
        }
    }

    @Override
    public void setLocation(World world, BlockPos pos) {
        super.setLocation(world, pos);

        if (!world.isClient()) {
            for (int i = 0; i < diskInventory.getSlotCount(); ++i) {
                setDiskState(i, diskInventory.getInvStack(i));
            }
        }
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);

        if (tag.contains("inv")) {
            diskInventory.fromTag(tag.getCompound("inv"));
        }
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag = super.toTag(tag);
        tag.put("inv", diskInventory.toTag());
        return tag;
    }

    public FixedItemInv getDiskInventory() {
        return diskInventory;
    }

    @Override
    public @Nullable Object getRenderAttachmentData() {
        return diskStates;
    }

    @Override
    public void onChange(FixedItemInvView view, int slot, ItemStack oldStack, ItemStack newStack) {
        if (!world.isClient()) {
            setDiskState(slot, newStack);
            sync();
        }
    }

    private void setDiskState(int slot, ItemStack stack) {
        storageDisks[slot] = null;

        if (!stack.isEmpty()) {
            StorageDiskItem.getId(stack).ifPresent(id -> storageDisks[slot] = RefinedStorage2Mod.API.getStorageDiskManager(world).getDisk(id).orElse(null));
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        ListTag list = tag.getList("states", NbtType.BYTE);
        for (int i = 0; i < list.size(); ++i) {
            diskStates.set(i, DiskState.values()[((ByteTag) list.get(i)).getInt()]);
        }
        BlockState state = world.getBlockState(pos);
        world.updateListeners(pos, state, state, 1 | 2);
    }

    private DiskState getDiskState(Optional<StorageDisk> disk) {
        return disk.map(d -> DiskState.NORMAL).orElse(DiskState.NONE);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        ListTag list = new ListTag();
        for (StorageDisk disk : storageDisks) {
            list.add(ByteTag.of((byte) getDiskState(Optional.ofNullable(disk)).ordinal()));
        }
        tag.put("states", list);
        return tag;
    }
}
