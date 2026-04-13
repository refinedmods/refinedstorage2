package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractStorageContainerNetworkNode;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.common.api.storage.StorageContainerItem;
import com.refinedmods.refinedstorage.common.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.common.support.FilteredContainer;

import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class DiskInventory extends FilteredContainer implements AbstractStorageContainerNetworkNode.Provider {
    private final Consumer<DiskInventory> listener;
    @Nullable
    private StorageRepository storageRepository;

    public DiskInventory(final Consumer<DiskInventory> listener, final int diskCount) {
        super(diskCount, StorageContainerItem.VALIDATOR);
        this.listener = listener;
    }

    public void setStorageRepository(@Nullable final StorageRepository storageRepository) {
        this.storageRepository = storageRepository;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        listener.accept(this);
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
}
