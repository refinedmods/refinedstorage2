package com.refinedmods.refinedstorage2.platform.fabric.item;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.disk.ListenableItemDiskStorage;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemStorageDiskItem extends StorageDiskItemImpl {
    private final ItemStorageType type;

    public ItemStorageDiskItem(Settings settings, ItemStorageType type) {
        super(settings);
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
        return Optional.of(new ItemStack(Rs2Mod.ITEMS.getStoragePart(type), count));
    }

    @Override
    protected StorageDisk<?> createStorageDisk(World world) {
        return new ListenableItemDiskStorage(type.getCapacity(), Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world)::markAsChanged);
    }

    @Override
    protected ItemStack createDisassemblyByproduct() {
        return new ItemStack(Rs2Mod.ITEMS.getStorageHousing());
    }

    public enum ItemStorageType {
        ONE_K("1k", 1000),
        FOUR_K("4k", 4000),
        SIXTEEN_K("16k", 16_000),
        SIXTY_FOUR_K("64k", 64_000),
        CREATIVE("creative", -1);

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
    }
}
