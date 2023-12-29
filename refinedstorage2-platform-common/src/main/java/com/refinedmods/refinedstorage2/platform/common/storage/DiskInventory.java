package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.network.impl.node.multistorage.MultiStorageProvider;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.TypedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class DiskInventory extends SimpleContainer implements MultiStorageProvider {
    private final DiskListener listener;
    @Nullable
    private StorageRepository storageRepository;

    public DiskInventory(final DiskListener listener, final int diskCount) {
        super(diskCount);
        this.listener = listener;
    }

    public void setStorageRepository(@Nullable final StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return stack.getItem() instanceof StorageContainerItem;
    }

    @Override
    public ItemStack removeItem(final int slot, final int amount) {
        // Forge InvWrapper calls this instead of setItem.
        final ItemStack result = super.removeItem(slot, amount);
        listener.onDiskChanged(slot);
        return result;
    }

    @Override
    public void setItem(final int slot, final ItemStack stack) {
        super.setItem(slot, stack);
        listener.onDiskChanged(slot);
    }

    @Override
    public <T> Optional<TypedStorage<T, Storage<T>>> resolve(final int index) {
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

    @FunctionalInterface
    public interface DiskListener {
        void onDiskChanged(int slot);
    }
}
