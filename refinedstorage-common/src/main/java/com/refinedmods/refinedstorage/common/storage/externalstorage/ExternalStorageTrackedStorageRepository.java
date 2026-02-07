package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

class ExternalStorageTrackedStorageRepository extends InMemoryTrackedStorageRepository {
    private static final Codec<ChangedByAt> SINGLE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceCodecs.CODEC.fieldOf("resource").forGetter(ChangedByAt::resource),
        Codec.STRING.fieldOf("by").forGetter(ChangedByAt::changedBy),
        Codec.LONG.fieldOf("at").forGetter(ChangedByAt::changedAt)
    ).apply(instance, ChangedByAt::new));
    public static final Codec<List<ChangedByAt>> CODEC = Codec.list(SINGLE_CODEC);

    private final Runnable listener;

    ExternalStorageTrackedStorageRepository(final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void update(final ResourceKey resource, final Actor actor, final long time) {
        super.update(resource, actor, time);
        listener.run();
    }

    List<ChangedByAt> getTrackedResources() {
        return trackedResourcesByActorType.getOrDefault(PlayerActor.class, Collections.emptyMap())
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey() instanceof PlatformResourceKey)
            .map(entry -> new ChangedByAt(
                (PlatformResourceKey) entry.getKey(),
                entry.getValue().getSourceName(),
                entry.getValue().getTime()
            ))
            .toList();
    }

    void load(final List<ChangedByAt> items) {
        // call super to avoid marking dirty
        items.forEach(item -> super.update(
            item.resource(),
            new PlayerActor(item.changedBy()),
            item.changedAt()
        ));
    }

    public record ChangedByAt(PlatformResourceKey resource, String changedBy, long changedAt) {
    }
}
