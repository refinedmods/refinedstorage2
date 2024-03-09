package com.refinedmods.refinedstorage2.platform.common.storage.externalstorage;

import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;

import java.util.Collections;
import java.util.Map;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;

class ExternalStorageTrackedStorageRepository extends InMemoryTrackedStorageRepository {
    private static final String TAG_MODIFIED_BY = "mb";
    private static final String TAG_MODIFIED_AT = "ma";
    private static final String TAG_RESOURCE_TYPE = "rt";

    private final Runnable listener;

    ExternalStorageTrackedStorageRepository(final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void update(final ResourceKey resource, final Actor actor, final long time) {
        super.update(resource, actor, time);
        listener.run();
    }

    ListTag toTag() {
        final ListTag items = new ListTag();
        getPersistentTrackedResources().forEach((resource, trackedResource) -> {
            if (!(resource instanceof PlatformResourceKey platformResource)) {
                return;
            }
            final ResourceType resourceType = platformResource.getResourceType();
            PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresent(id -> {
                final CompoundTag tag = platformResource.toTag();
                tag.putString(TAG_MODIFIED_BY, trackedResource.getSourceName());
                tag.putLong(TAG_MODIFIED_AT, trackedResource.getTime());
                tag.putString(TAG_RESOURCE_TYPE, id.toString());
                items.add(tag);
            });
        });
        return items;
    }

    void fromTag(final ListTag items) {
        items.forEach(tag -> {
            final ResourceLocation resourceTypeId = new ResourceLocation(
                ((CompoundTag) tag).getString(TAG_RESOURCE_TYPE)
            );
            fromTag((CompoundTag) tag, resourceTypeId);
        });
    }

    private void fromTag(final CompoundTag tag, final ResourceLocation resourceTypeId) {
        PlatformApi.INSTANCE.getResourceTypeRegistry()
            .get(resourceTypeId)
            .flatMap(resourceType -> resourceType.fromTag(tag))
            .ifPresent(resource -> {
                final String modifiedBy = tag.getString(TAG_MODIFIED_BY);
                final long modifiedAt = tag.getLong(TAG_MODIFIED_AT);
                // Call super here to avoid marking dirty.
                super.update(resource, new PlayerActor(modifiedBy), modifiedAt);
            });
    }

    private Map<ResourceKey, TrackedResource> getPersistentTrackedResources() {
        return trackedResourcesByActorType.getOrDefault(
            PlayerActor.class,
            Collections.emptyMap()
        );
    }
}
