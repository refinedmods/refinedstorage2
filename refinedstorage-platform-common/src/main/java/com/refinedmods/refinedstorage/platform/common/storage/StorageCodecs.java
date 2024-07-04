package com.refinedmods.refinedstorage.platform.common.storage;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Storage;
import com.refinedmods.refinedstorage.api.storage.limited.LimitedStorage;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedStorage;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class StorageCodecs {
    private static final StreamCodec<RegistryFriendlyByteBuf, TrackedResource> TRACKED_RESOURCE_STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, TrackedResource::getSourceName,
            ByteBufCodecs.VAR_LONG, TrackedResource::getTime,
            TrackedResource::new
        );
    private static final Codec<StorageChangedByAt> CHANGED_BY_AT_CODEC =
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("changedBy").forGetter(StorageChangedByAt::changedBy),
            Codec.LONG.fieldOf("changedAt").forGetter(StorageChangedByAt::changedAt)
        ).apply(instance, StorageChangedByAt::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<TrackedResource>>
        TRACKED_RESOURCE_OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(TRACKED_RESOURCE_STREAM_CODEC);

    private StorageCodecs() {
    }

    static <T extends ResourceKey> MapCodec<StorageData<T>> homogeneousStorageData(final Codec<T> resourceCodec) {
        final Codec<StorageResource<T>> storageResourceCodec = RecordCodecBuilder.create(instance -> instance.group(
            resourceCodec.fieldOf("resource").forGetter(StorageResource::resource),
            Codec.LONG.fieldOf("amount").forGetter(StorageResource::amount),
            Codec.optionalField("changed", CHANGED_BY_AT_CODEC, false).forGetter(StorageResource::changed)
        ).apply(instance, StorageResource::new));

        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.optionalField("capacity", Codec.LONG, false).forGetter(StorageData::capacity),
            Codec.list(storageResourceCodec).fieldOf("resources").forGetter(StorageData::resources)
        ).apply(instance, StorageData::new));
    }

    record StorageData<T extends ResourceKey>(Optional<Long> capacity, List<StorageResource<T>> resources) {
        static <T extends ResourceKey> StorageData<T> empty(@Nullable final Long capacity) {
            return new StorageData<>(Optional.ofNullable(capacity), List.of());
        }

        static <T extends ResourceKey> StorageData<T> ofHomogeneousStorage(
            final Storage storage,
            final Predicate<ResourceKey> valid,
            final Function<ResourceKey, T> caster
        ) {
            final Optional<Long> capacity = storage instanceof LimitedStorage limitedStorage
                ? Optional.of(limitedStorage.getCapacity())
                : Optional.empty();
            final List<StorageResource<T>> resources = storage.getAll().stream()
                .filter(resourceAmount -> valid.test(resourceAmount.getResource()))
                .map(resourceAmount -> getResource(storage, caster, resourceAmount))
                .toList();
            return new StorageData<>(capacity, resources);
        }

        private static <T extends ResourceKey> StorageResource<T> getResource(
            final Storage storage,
            final Function<ResourceKey, T> caster,
            final ResourceAmount resourceAmount
        ) {
            return new StorageResource<>(
                caster.apply(resourceAmount.getResource()),
                resourceAmount.getAmount(),
                getChanged(storage, resourceAmount)
            );
        }

        private static Optional<StorageChangedByAt> getChanged(final Storage storage,
                                                               final ResourceAmount resourceAmount) {
            if (!(storage instanceof TrackedStorage trackedStorage)) {
                return Optional.empty();
            }
            return trackedStorage.findTrackedResourceByActorType(resourceAmount.getResource(), PlayerActor.class)
                .map(StorageChangedByAt::ofTrackedResource);
        }
    }

    record StorageResource<T extends ResourceKey>(T resource, long amount, Optional<StorageChangedByAt> changed) {
    }

    record StorageChangedByAt(String changedBy, long changedAt) {
        private static StorageChangedByAt ofTrackedResource(final TrackedResource trackedResource) {
            return new StorageChangedByAt(trackedResource.getSourceName(), trackedResource.getTime());
        }
    }
}
