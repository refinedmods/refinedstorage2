package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformLimitedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;

import java.util.Optional;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemStorageDiskItem extends StorageDiskItemImpl {
    private final ItemStorageType.Variant variant;

    public ItemStorageDiskItem(Item.Properties properties, ItemStorageType.Variant variant) {
        super(properties);
        this.variant = variant;
    }

    @Override
    public Optional<StorageChannelType<?>> getType(ItemStack stack) {
        return Optional.of(StorageChannelTypes.ITEM);
    }

    @Override
    protected Storage<?> createStorage(Level level) {
        TrackedStorageRepository<ItemResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (!variant.hasCapacity()) {
            return new PlatformStorage<>(
                    new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                    com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType.INSTANCE,
                    trackingRepository,
                    Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
            );
        }
        return new PlatformLimitedStorage<>(
                new LimitedStorageImpl<>(
                        new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                        variant.getCapacity()
                ),
                com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType.INSTANCE,
                trackingRepository,
                Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
        );
    }

    @Override
    protected ItemStack createPrimaryDisassemblyByproduct() {
        return new ItemStack(Items.INSTANCE.getStorageHousing());
    }

    @Override
    protected ItemStack createSecondaryDisassemblyByproduct(int count) {
        if (variant == ItemStorageType.Variant.CREATIVE) {
            return null;
        }
        return new ItemStack(Items.INSTANCE.getStoragePart(variant), count);
    }
}
