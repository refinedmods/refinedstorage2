package com.refinedmods.refinedstorage.platform.common.storage.externalstorage;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.tracked.InMemoryTrackedStorageRepository;
import com.refinedmods.refinedstorage.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

class ExternalStorageTrackedStorageRepository extends InMemoryTrackedStorageRepository {
    private static final Codec<ChangedByAt> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceCodecs.CODEC.fieldOf("resource").forGetter(ChangedByAt::resource),
        Codec.STRING.fieldOf("changedBy").forGetter(ChangedByAt::changedBy),
        Codec.LONG.fieldOf("changedAt").forGetter(ChangedByAt::changedAt)
    ).apply(instance, ChangedByAt::new));
    private static final Codec<List<ChangedByAt>> LIST_CODEC = Codec.list(CODEC);

    private final Runnable listener;

    ExternalStorageTrackedStorageRepository(final Runnable listener) {
        this.listener = listener;
    }

    @Override
    public void update(final ResourceKey resource, final Actor actor, final long time) {
        super.update(resource, actor, time);
        listener.run();
    }

    Tag toTag(final HolderLookup.Provider provider) {
        return LIST_CODEC.encode(
            getTrackedResources(),
            provider.createSerializationContext(NbtOps.INSTANCE),
            new ListTag()
        ).getOrThrow();
    }

    private List<ChangedByAt> getTrackedResources() {
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

    void fromTag(final Tag tag, final HolderLookup.Provider provider) {
        LIST_CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), tag).ifSuccess(
            result -> result.getFirst().forEach(
                // call super to avoid marking dirty
                changedByAt -> super.update(
                    changedByAt.resource(),
                    new PlayerActor(changedByAt.changedBy()),
                    changedByAt.changedAt()
                )));
    }

    private record ChangedByAt(PlatformResourceKey resource, String changedBy, long changedAt) {
    }
}
