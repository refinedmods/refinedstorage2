package com.refinedmods.refinedstorage2.platform.api.storage.channel;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;

import java.util.Optional;
import java.util.function.BiConsumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public abstract class AbstractPlatformStorageChannelType<T> implements PlatformStorageChannelType<T> {
    private static final String TAG_CHANGED_BY = "cb";
    private static final String TAG_CHANGED_AT = "ca";

    private final String name;
    private final StorageChannelType<T> delegate;
    private final MutableComponent title;
    private final ResourceLocation textureIdentifier;
    private final int textureX;
    private final int textureY;

    protected AbstractPlatformStorageChannelType(final String name,
                                                 final StorageChannelType<T> delegate,
                                                 final MutableComponent title,
                                                 final ResourceLocation textureIdentifier,
                                                 final int textureX,
                                                 final int textureY) {
        this.name = name;
        this.delegate = CoreValidations.validateNotNull(delegate, "Delegate cannot be null");
        this.title = title;
        this.textureIdentifier = textureIdentifier;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    @Override
    public CompoundTag toTag(final T resource, final TrackedResource trackedResource) {
        final CompoundTag tag = toTag(resource);
        tag.putString(TAG_CHANGED_BY, trackedResource.getSourceName());
        tag.putLong(TAG_CHANGED_AT, trackedResource.getTime());
        return tag;
    }

    protected abstract CompoundTag toTag(T resource);

    @Override
    public void fromTag(final CompoundTag tag, final BiConsumer<T, TrackedResource> acceptor) {
        fromTag(tag).ifPresent(resource -> {
            final String changedBy = tag.getString(TAG_CHANGED_BY);
            final long changedAt = tag.getLong(TAG_CHANGED_AT);
            acceptor.accept(resource, new TrackedResource(changedBy, changedAt));
        });
    }

    protected abstract Optional<T> fromTag(CompoundTag tag);

    @Override
    public StorageChannel<T> create() {
        return delegate.create();
    }

    @Override
    public MutableComponent getTitle() {
        return title;
    }

    @Override
    public ResourceLocation getTextureIdentifier() {
        return textureIdentifier;
    }

    @Override
    public int getXTexture() {
        return textureX;
    }

    @Override
    public int getYTexture() {
        return textureY;
    }

    @Override
    public String toString() {
        return name;
    }
}
