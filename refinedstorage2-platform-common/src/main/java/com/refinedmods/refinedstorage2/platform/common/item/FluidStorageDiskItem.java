package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FluidStorageDiskItem extends StorageDiskItemImpl {
    private final FluidStorageType type;

    public FluidStorageDiskItem(Item.Properties properties, FluidStorageType type) {
        super(properties);
        this.type = type;
    }

    @Override
    protected String formatQuantity(long qty) {
        return Platform.INSTANCE.getBucketQuantityFormatter().formatWithUnits(qty);
    }

    @Override
    public Optional<StorageChannelType<?>> getType(ItemStack stack) {
        return Optional.of(StorageChannelTypes.FLUID);
    }

    @Override
    protected Optional<ItemStack> createStoragePart(int count) {
        if (type == FluidStorageType.CREATIVE) {
            return Optional.empty();
        }
        return Optional.of(new ItemStack(Items.INSTANCE.getFluidStoragePart(type), count));
    }

    @Override
    protected Storage<?> createStorage(Level level) {
        TrackedStorageRepository<FluidResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (!type.hasCapacity()) {
            return new PlatformStorage<>(
                    new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                    com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType.INSTANCE,
                    trackingRepository,
                    Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
            );
        }
        return new PlatformCappedStorage<>(
                new CappedStorage<>(
                        new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
                        type.getBuckets() * Platform.INSTANCE.getBucketAmount()
                ),
                com.refinedmods.refinedstorage2.platform.common.internal.storage.type.FluidStorageType.INSTANCE,
                trackingRepository,
                Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
        );
    }

    @Override
    protected ItemStack createDisassemblyByproduct() {
        return new ItemStack(Items.INSTANCE.getStorageHousing());
    }

    public enum FluidStorageType {
        SIXTY_FOUR_B("64b", 64),
        TWO_HUNDRED_FIFTY_SIX_B("256b", 256),
        THOUSAND_TWENTY_FOUR_B("1024b", 1024),
        FOUR_THOUSAND_NINETY_SIX_B("4096b", 4096),
        CREATIVE("creative", 0);

        private final String name;
        private final long buckets;

        FluidStorageType(String name, long buckets) {
            this.name = name;
            this.buckets = buckets;
        }

        public String getName() {
            return name;
        }

        public long getBuckets() {
            return buckets;
        }

        public boolean hasCapacity() {
            return buckets > 0;
        }
    }
}
