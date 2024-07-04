package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.StorageState;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.common.support.FilteredContainer;

import java.util.Optional;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

public class DiskInventory extends FilteredContainer implements AbstractStorageContainerNetworkNode.Provider {
    private static final String TAG_DISK_STATE = "s";
    private static final String TAG_DISK_ITEM_ID = "i";

    private final DiskListener listener;
    @Nullable
    private StorageRepository storageRepository;

    public DiskInventory(final DiskListener listener, final int diskCount) {
        super(diskCount, StorageContainerItem.stackValidator());
        this.listener = listener;
    }

    public void setStorageRepository(@Nullable final StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        // Forge InvWrapper calls this instead of setItem.
        final ItemStack result = super.removeItem(slot, amount);
        listener.onDiskChanged(this, slot);
        return result;
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        super.setItem(slot, stack);
        listener.onDiskChanged(this, slot);
    }

    @Override
    public Optional<Storage> resolve(final int index) {
        if (storageRepository == null) {
            return Optional.empty();
        }
        return validateAndGetStack(index).flatMap(stack -> ((StorageContainerItem) stack.getItem()).resolve(
            storageRepository,
            stack
        ));
    }

    private Optional<ItemStack> validateAndGetStack(final int slot) {
        final ItemStack stack = getItem(slot);
        if (stack.isEmpty() || !(stack.getItem() instanceof StorageContainerItem)) {
            return Optional.empty();
        }
        return Optional.of(stack);
    }

    public ListTag toSyncTag(final IntFunction<StorageState> stateProvider) {
        final ListTag list = new ListTag();
        for (int i = 0; i < getContainerSize(); ++i) {
            final CompoundTag disk = new CompoundTag();
            disk.putByte(TAG_DISK_STATE, (byte) stateProvider.apply(i).ordinal());
            final ItemStack diskItem = getItem(i);
            if (!diskItem.isEmpty()) {
                disk.putInt(TAG_DISK_ITEM_ID, BuiltInRegistries.ITEM.getId(diskItem.getItem()));
            }
            list.add(disk);
        }
        return list;
    }

    public Disk[] fromSyncTag(final ListTag list) {
        final Disk[] disks = new Disk[list.size()];
        for (int i = 0; i < list.size(); ++i) {
            final CompoundTag diskTag = list.getCompound(i);
            disks[i] = BuiltInRegistries.ITEM.getHolder(diskTag.getInt(TAG_DISK_ITEM_ID))
                .map(item -> new Disk(item.value(), getState(diskTag)))
                .orElse(new Disk(null, StorageState.NONE));
        }
        return disks;
    }

    private StorageState getState(final CompoundTag tag) {
        final int stateOrdinal = tag.getByte(TAG_DISK_STATE);
        final StorageState[] values = StorageState.values();
        if (stateOrdinal < 0 || stateOrdinal >= values.length) {
            return StorageState.NONE;
        }
        return values[stateOrdinal];
    }

    @FunctionalInterface
    public interface DiskListener {
        void onDiskChanged(DiskInventory inventory, int slot);
    }
}
