package com.refinedmods.refinedstorage2.platform.fabric.item;

import com.refinedmods.refinedstorage2.api.storage.CappedStorage;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.abstractions.PlatformAbstractions;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.item.StorageDiskItemImpl;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformCappedStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.PlatformStorage;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.DropletsQuantityFormatter;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.channel.StorageChannelTypes;

import java.util.Optional;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FluidStorageDiskItem extends StorageDiskItemImpl {
    private final FluidStorageType type;

    public FluidStorageDiskItem(Properties properties, FluidStorageType type) {
        super(properties);
        this.type = type;
    }

    @Override
    protected String formatQuantity(long qty) {
        return DropletsQuantityFormatter.formatAsBucketWithUnits(qty);
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
        return Optional.of(new ItemStack(Rs2Mod.ITEMS.getFluidStoragePart(type), count));
    }

    @Override
    protected Storage<?> createStorage(Level level) {
        if (!type.hasCapacity()) {
            return new PlatformStorage<>(
                    new InMemoryStorageImpl<>(),
                    com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.FluidStorageType.INSTANCE,
                    Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
            );
        }
        return new PlatformCappedStorage<>(
                new CappedStorage<>(type.getBuckets() * PlatformAbstractions.INSTANCE.getBucketAmount()),
                com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.FluidStorageType.INSTANCE,
                Rs2PlatformApiFacade.INSTANCE.getStorageRepository(level)::markAsChanged
        );
    }

    @Override
    protected ItemStack createDisassemblyByproduct() {
        return new ItemStack(Rs2Mod.ITEMS.getStorageHousing());
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
