package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.InMemoryStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage2.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceTypes;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class FluidStorageType implements StorageType {
    private static final String TAG_CAPACITY = "cap";
    private static final String TAG_STACKS = "stacks";
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_CHANGED_BY = "cb";
    private static final String TAG_CHANGED_AT = "ca";

    FluidStorageType() {
    }

    @Override
    public Storage create(@Nullable final Long capacity, final Runnable listener) {
        return innerCreate(capacity, listener);
    }

    @Override
    public Storage fromTag(final CompoundTag tag, final Runnable listener) {
        final PlatformStorage storage = innerCreate(
            tag.contains(TAG_CAPACITY) ? tag.getLong(TAG_CAPACITY) : null,
            listener
        );
        final ListTag stacks = tag.getList(TAG_STACKS, Tag.TAG_COMPOUND);
        for (final Tag stackTag : stacks) {
            ResourceTypes.FLUID.fromTag((CompoundTag) stackTag).ifPresent(resource -> storage.load(
                resource,
                ((CompoundTag) stackTag).getLong(TAG_AMOUNT),
                ((CompoundTag) stackTag).getString(TAG_CHANGED_BY),
                ((CompoundTag) stackTag).getLong(TAG_CHANGED_AT)
            ));
        }
        return storage;
    }

    private PlatformStorage innerCreate(@Nullable final Long capacity, final Runnable listener) {
        final TrackedStorageRepository trackingRepository = new InMemoryTrackedStorageRepository();
        if (capacity != null) {
            final LimitedStorageImpl delegate = new LimitedStorageImpl(
                new TrackedStorageImpl(
                    new InMemoryStorageImpl(),
                    trackingRepository,
                    System::currentTimeMillis
                ),
                capacity
            );
            return new LimitedPlatformStorage(
                delegate,
                StorageTypes.FLUID,
                trackingRepository,
                listener
            );
        }
        return new PlatformStorage(
            new TrackedStorageImpl(new InMemoryStorageImpl(), trackingRepository, System::currentTimeMillis),
            StorageTypes.FLUID,
            trackingRepository,
            listener
        );
    }

    @Override
    public CompoundTag toTag(final Storage storage) {
        final CompoundTag tag = new CompoundTag();
        if (storage instanceof LimitedStorage limitedStorage) {
            tag.putLong(TAG_CAPACITY, limitedStorage.getCapacity());
        }
        final ListTag stacks = new ListTag();
        for (final ResourceAmount resourceAmount : storage.getAll()) {
            stacks.add(toTag(storage, resourceAmount));
        }
        tag.put(TAG_STACKS, stacks);
        return tag;
    }

    private CompoundTag toTag(final Storage storage, final ResourceAmount resourceAmount) {
        if (!(resourceAmount.getResource() instanceof FluidResource fluidResource)) {
            throw new UnsupportedOperationException();
        }
        final CompoundTag tag = fluidResource.toTag();
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        if (storage instanceof TrackedStorage trackedStorage) {
            trackedStorage
                .findTrackedResourceByActorType(resourceAmount.getResource(), PlayerActor.class)
                .ifPresent(trackedResource -> {
                    tag.putString(TAG_CHANGED_BY, trackedResource.getSourceName());
                    tag.putLong(TAG_CHANGED_AT, trackedResource.getTime());
                });
        }
        return tag;
    }

    @Override
    public boolean isAllowed(final ResourceKey resource) {
        return resource instanceof FluidResource;
    }

    public enum Variant {
        SIXTY_FOUR_B("64b", 64L),
        TWO_HUNDRED_FIFTY_SIX_B("256b", 256L),
        THOUSAND_TWENTY_FOUR_B("1024b", 1024L),
        FOUR_THOUSAND_NINETY_SIX_B("4096b", 4096L),
        CREATIVE("creative", null);

        private final String name;
        @Nullable
        private final Long capacityInBuckets;

        Variant(final String name, @Nullable final Long capacityInBuckets) {
            this.name = name;
            this.capacityInBuckets = capacityInBuckets;
        }

        public String getName() {
            return name;
        }

        @Nullable
        public Long getCapacity() {
            if (capacityInBuckets == null) {
                return null;
            }
            return capacityInBuckets * Platform.INSTANCE.getBucketAmount();
        }

        public boolean hasCapacity() {
            return capacityInBuckets != null;
        }
    }
}
