package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.minecraft.nbt.CompoundTag;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public class NamedStorageChannelType<T> implements PlatformStorageChannelType<T> {
    private static final String TAG_CHANGED_BY = "cb";
    private static final String TAG_CHANGED_AT = "ca";

    private final String name;
    private final StorageChannelType<T> delegate;
    private final Function<T, CompoundTag> serializer;
    private final Function<CompoundTag, Optional<T>> deserializer;

    public NamedStorageChannelType(final String name,
                                   final StorageChannelType<T> delegate,
                                   final Function<T, CompoundTag> serializer,
                                   final Function<CompoundTag, Optional<T>> deserializer) {
        this.name = name;
        this.delegate = CoreValidations.validateNotNull(delegate, "Delegate cannot be null");
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Override
    public CompoundTag toTag(final T resource, final TrackedResource trackedResource) {
        final CompoundTag tag = serializer.apply(resource);
        tag.putString(TAG_CHANGED_BY, trackedResource.getSourceName());
        tag.putLong(TAG_CHANGED_AT, trackedResource.getTime());
        return tag;
    }

    @Override
    public void fromTag(final CompoundTag tag, final BiConsumer<T, TrackedResource> acceptor) {
        deserializer.apply(tag).ifPresent(resource -> {
            final String changedBy = tag.getString(TAG_CHANGED_BY);
            final long changedAt = tag.getLong(TAG_CHANGED_AT);
            acceptor.accept(resource, new TrackedResource(changedBy, changedAt));
        });
    }

    @Override
    public StorageChannel<T> create() {
        return delegate.create();
    }

    @Override
    public String toString() {
        return name;
    }
}
