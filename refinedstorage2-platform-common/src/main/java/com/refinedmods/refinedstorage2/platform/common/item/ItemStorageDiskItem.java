package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemStorageDiskItem extends StorageDiskItemImpl {
    private final ItemStorageType type;

    public ItemStorageDiskItem(Item.Properties properties, ItemStorageType type) {
        super(properties);
        this.type = type;
    }

    @Override
    public Optional<StorageChannelType<?>> getType(ItemStack stack) {
        return Optional.of(StorageChannelTypes.ITEM);
    }

    @Override
    protected Optional<ItemStack> createStoragePart(int count) {
        if (type == ItemStorageType.CREATIVE) {
            return Optional.empty();
        }
        return Optional.of(new ItemStack(Items.INSTANCE.getStoragePart(type), count));
    }

    @Override
    protected Storage<?> createStorage(Level level) {
        if (!type.hasCapacity()) {
            return new PlatformStorage<>(
                    new InMemoryStorageImpl<>(),
                    com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType.INSTANCE,
                    Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
            );
        }
        return new PlatformCappedStorage<>(
                new CappedStorage<>(type.getCapacity()),
                com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType.INSTANCE,
                Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
        );
    }

    @Override
    protected ItemStack createDisassemblyByproduct() {
        return new ItemStack(Items.INSTANCE.getStorageHousing());
    }

    public enum ItemStorageType {
        ONE_K("1k", 1000),
        FOUR_K("4k", 4000),
        SIXTEEN_K("16k", 16_000),
        SIXTY_FOUR_K("64k", 64_000),
        CREATIVE("creative", 0);

        private final String name;
        private final int capacity;

        ItemStorageType(String name, int capacity) {
            this.name = name;
            this.capacity = capacity;
        }

        public String getName() {
            return name;
        }

        public int getCapacity() {
            return capacity;
        }

        public boolean hasCapacity() {
            return capacity > 0;
        }
    }
}
