package com.refinedmods.refinedstorage2.fabric.item;

import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.api.storage.disk.StorageDisk;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.FabricItemDiskStorage;
import com.refinedmods.refinedstorage2.fabric.api.storage.disk.FabricStorageDiskManager;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemStorageDiskItem extends AbstractStorageDiskItem {
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
        return new FabricItemDiskStorage(type.getCapacity(),
                () -> ((FabricStorageDiskManager) Rs2PlatformApiFacade.INSTANCE.getStorageDiskManager(world)).markDirty());
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
