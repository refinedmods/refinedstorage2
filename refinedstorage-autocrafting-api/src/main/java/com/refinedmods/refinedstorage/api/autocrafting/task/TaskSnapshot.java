package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceListImpl;
import com.refinedmods.refinedstorage.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage.api.storage.Actor;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

public record TaskSnapshot(
    TaskId id,
    ResourceKey resource,
    long amount,
    Actor actor,
    boolean notifyActor,
    long startTime,
    Map<Pattern, PatternSnapshot> patterns,
    List<PatternSnapshot> completedPatterns,
    ResourceList initialRequirements,
    ResourceList internalStorage,
    TaskState state,
    boolean cancelled
) {
    MutableResourceList copyInitialRequirements() {
        final MutableResourceList copy = MutableResourceListImpl.create();
        initialRequirements.getAll().forEach(key -> copy.add(key, initialRequirements.get(key)));
        return copy;
    }

    public MutableResourceList copyInternalStorage() {
        final MutableResourceList copy = MutableResourceListImpl.create();
        internalStorage.getAll().forEach(key -> copy.add(key, internalStorage.get(key)));
        return copy;
    }

    public record PatternSnapshot(
        boolean root,
        Pattern pattern,
        Map<Integer, Map<ResourceKey, Long>> ingredients,
        @Nullable InternalPatternSnapshot internalPattern,
        @Nullable ExternalPatternSnapshot externalPattern
    ) {
        AbstractTaskPattern toTaskPattern() {
            return internalPattern != null ? new InternalTaskPattern(this) : new ExternalTaskPattern(this);
        }
    }

    public record InternalPatternSnapshot(long originalIterationsRemaining, long iterationsRemaining) {
    }

    public record ExternalPatternSnapshot(
        ResourceList expectedOutputs,
        ResourceList simulatedIterationInputs,
        long originalIterationsToSendToSink,
        long iterationsToSendToSink,
        long iterationsReceived,
        boolean interceptedAnythingSinceLastStep,
        ExternalPatternSink.@Nullable Result lastSinkResult,
        @Nullable
        ExternalPatternSinkKey lastSinkResultKey
    ) {
        MutableResourceList copyExpectedOutputs() {
            final MutableResourceList copy = MutableResourceListImpl.create();
            expectedOutputs.getAll().forEach(key -> copy.add(key, expectedOutputs.get(key)));
            return copy;
        }
    }
}
