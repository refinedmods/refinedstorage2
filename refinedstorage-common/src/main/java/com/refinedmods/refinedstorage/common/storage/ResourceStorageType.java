package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.StorageImpl;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageImpl;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorageRepository;
import com.refinedmods.refinedstorage.common.api.storage.SerializableStorage;
import com.refinedmods.refinedstorage.common.api.storage.StorageContents;
import com.refinedmods.refinedstorage.common.api.storage.StorageType;
import com.refinedmods.refinedstorage.common.support.ErrorHandlingListCodec;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

public class ResourceStorageType implements StorageType {
    private static final String DESERIALIZE_ERROR_MESSAGE = """
        Refined Storage could not load a resource in storage.
        This could be because the resource no longer exists after a mod update, or if the data format of the
        resource has changed. In any case, this is NOT caused by Refined Storage.
        Refined Storage will try to gracefully handle this problem and continue to load the storage data.
        The problematic resource might end up being removed from storage, or may no longer have any additional data
        associated with it.
        Error message:""";
    private static final Codec<StorageContents.Changed> CHANGED_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("by").forGetter(StorageContents.Changed::by),
            Codec.LONG.fieldOf("at").forGetter(StorageContents.Changed::at)
        ).apply(instance, StorageContents.Changed::new));

    private final MapCodec<StorageContents> codec;
    private final Predicate<ResourceKey> valid;
    private final long diskInterfaceTransferQuota;
    private final long diskInterfaceTransferQuotaWithStackUpgrade;

    public ResourceStorageType(final Codec<ResourceKey> resourceCodec, final Predicate<ResourceKey> valid,
                               final long diskInterfaceTransferQuota,
                               final long diskInterfaceTransferQuotaWithStackUpgrade) {
        this.valid = valid;
        this.diskInterfaceTransferQuota = diskInterfaceTransferQuota;
        this.diskInterfaceTransferQuotaWithStackUpgrade = diskInterfaceTransferQuotaWithStackUpgrade;

        final Codec<StorageContents.Stored> storedCodec = RecordCodecBuilder.create(instance -> instance.group(
            resourceCodec.fieldOf("resource").forGetter(StorageContents.Stored::resource),
            Codec.LONG.fieldOf("amount").forGetter(StorageContents.Stored::amount),
            Codec.optionalField("changed", CHANGED_CODEC, false).forGetter(StorageContents.Stored::changed)
        ).apply(instance, StorageContents.Stored::new));

        this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.optionalField("capacity", Codec.LONG, false).forGetter(StorageContents::capacity),
            new ErrorHandlingListCodec<>(storedCodec, DESERIALIZE_ERROR_MESSAGE)
                .fieldOf("resources").forGetter(StorageContents::stored)
        ).apply(instance, (capacity, stored) -> new StorageContents(this, capacity, stored)));
    }

    @Override
    public SerializableStorage create(@Nullable final Long capacity, final Runnable listener) {
        return createStorage(createEmptyStorageContents(capacity), listener);
    }

    @Override
    public SerializableStorage create(final StorageContents contents, final Runnable listener) {
        return createStorage(contents, listener);
    }

    @Override
    public MapCodec<StorageContents> getCodec() {
        return codec;
    }

    @Override
    public boolean isAllowed(final ResourceKey resource) {
        return valid.test(resource);
    }

    @Override
    public long getDiskInterfaceTransferQuota(final boolean stackUpgrade) {
        if (stackUpgrade) {
            return diskInterfaceTransferQuotaWithStackUpgrade;
        }
        return diskInterfaceTransferQuota;
    }

    private SerializableStorage createStorage(final StorageContents contents, final Runnable listener) {
        final TrackedStorageRepository trackingRepository = new InMemoryTrackedStorageRepository();
        final TrackedStorageImpl tracked = new TrackedStorageImpl(
            new StorageImpl(),
            trackingRepository,
            System::currentTimeMillis
        );
        final PlatformStorage storage = contents.capacity().map(capacity -> {
            final LimitedStorageImpl limited = new LimitedStorageImpl(tracked, capacity);
            return (PlatformStorage) new LimitedPlatformStorage(limited, this, trackingRepository, listener);
        }).orElseGet(() -> new PlatformStorage(tracked, this, trackingRepository, listener));
        contents.stored().forEach(storage::load);
        return storage;
    }

    private StorageContents createEmptyStorageContents(@Nullable final Long capacity) {
        return new StorageContents(this, Optional.ofNullable(capacity), List.of());
    }
}
