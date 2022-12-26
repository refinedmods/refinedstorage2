package com.refinedmods.refinedstorage2.platform.common.block.entity.externalstorage;

import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;

import java.util.Collections;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

class ExternalStorageTrackedStorageRepository<T> extends InMemoryTrackedStorageRepository<T> {
    private final PlatformStorageChannelType<T> type;
    private final Runnable listener;

    ExternalStorageTrackedStorageRepository(final Runnable listener, final PlatformStorageChannelType<T> type) {
        this.listener = listener;
        this.type = type;
    }

    @Override
    public void update(final T resource, final Actor actor, final long time) {
        super.update(resource, actor, time);
        listener.run();
    }

    ListTag toTag() {
        final ListTag items = new ListTag();
        getPersistentTrackedResources().forEach((resource, trackedResource) -> {
            final CompoundTag tag = type.toTag(resource, trackedResource);
            items.add(tag);
        });
        return items;
    }

    void fromTag(final ListTag items) {
        items.forEach(tag -> type.fromTag(
                (CompoundTag) tag,
            // call super here to avoid marking dirty.
            (resource, trackedResource) -> super.update(
                resource,
                new PlayerActor(trackedResource.getSourceName()),
                trackedResource.getTime()
            )
        ));
    }

    private Map<T, TrackedResource> getPersistentTrackedResources() {
        return trackedResourcesByActorType.getOrDefault(
            PlayerActor.class,
            Collections.emptyMap()
        );
    }
}
