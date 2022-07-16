package com.refinedmods.refinedstorage2.platform.apiimpl.storage.type;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.LimitedPlatformStorage;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.PlatformStorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class FluidStorageType implements StorageType<FluidResource> {
    public static final FluidStorageType INSTANCE = new FluidStorageType();

    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";
    private static final String TAG_CHANGED_BY = "cb";
    private static final String TAG_CHANGED_AT = "ca";

    private FluidStorageType() {
    }

    @Override
    public Storage<FluidResource> fromTag(final CompoundTag tag, final Runnable listener) {
        final PlatformStorage<FluidResource> storage = createStorage(tag, listener);
        final ListTag stacks = tag.getList(TAG_STACKS, Tag.TAG_COMPOUND);
        for (final Tag stackTag : stacks) {
            FluidResource
                .fromTagWithAmount((CompoundTag) stackTag)
                .ifPresent(resourceAmount -> storage.load(
                    resourceAmount.getResource(),
                    resourceAmount.getAmount(),
                    ((CompoundTag) stackTag).getString(TAG_CHANGED_BY),
                    ((CompoundTag) stackTag).getLong(TAG_CHANGED_AT)
                ));
        }
        return storage;
    }

    private PlatformStorage<FluidResource> createStorage(final CompoundTag tag, final Runnable listener) {
        final TrackedStorageRepository<FluidResource> trackingRepository = new InMemoryTrackedStorageRepository<>();
        if (tag.contains(TAG_CAPACITY)) {
            final LimitedStorageImpl<FluidResource> delegate = new LimitedStorageImpl<>(
                new TrackedStorageImpl<>(
                    new InMemoryStorageImpl<>(),
                    trackingRepository,
                    System::currentTimeMillis
                ),
                tag.getLong(TAG_CAPACITY)
            );
            return new LimitedPlatformStorage<>(
                delegate,
                FluidStorageType.INSTANCE,
                trackingRepository,
                listener
            );
        }
        return new PlatformStorage<>(
            new TrackedStorageImpl<>(new InMemoryStorageImpl<>(), trackingRepository, System::currentTimeMillis),
            FluidStorageType.INSTANCE,
            trackingRepository,
            listener
        );
    }

    @Override
    public CompoundTag toTag(final Storage<FluidResource> storage) {
        final CompoundTag tag = new CompoundTag();
        if (storage instanceof LimitedStorage<?> limitedStorage) {
            tag.putLong(TAG_CAPACITY, limitedStorage.getCapacity());
        }
        final ListTag stacks = new ListTag();
        for (final ResourceAmount<FluidResource> resourceAmount : storage.getAll()) {
            stacks.add(toTag(storage, resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }

    private CompoundTag toTag(final Storage<FluidResource> storage,
                              final ResourceAmount<FluidResource> resourceAmount) {
        final CompoundTag tag = FluidResource.toTagWithAmount(resourceAmount);
        if (storage instanceof TrackedStorage<FluidResource> trackedStorage) {
            trackedStorage
                .findTrackedResourceByActorType(resourceAmount.getResource(), PlayerActor.class)
                .ifPresent(trackedResource -> {
                    tag.putString(TAG_CHANGED_BY, trackedResource.getSourceName());
                    tag.putLong(TAG_CHANGED_AT, trackedResource.getTime());
                });
        }
        return tag;
    }

    public enum Variant {
        SIXTY_FOUR_B("64b", 64),
        TWO_HUNDRED_FIFTY_SIX_B("256b", 256),
        THOUSAND_TWENTY_FOUR_B("1024b", 1024),
        FOUR_THOUSAND_NINETY_SIX_B("4096b", 4096),
        CREATIVE("creative", 0);

        private final String name;
        private final long capacityInBuckets;

        Variant(final String name, final long capacityInBuckets) {
            this.name = name;
            this.capacityInBuckets = capacityInBuckets;
        }

        public String getName() {
            return name;
        }

        public long getCapacityInBuckets() {
            return capacityInBuckets;
        }

        public boolean hasCapacity() {
            return capacityInBuckets > 0;
        }
    }
}
