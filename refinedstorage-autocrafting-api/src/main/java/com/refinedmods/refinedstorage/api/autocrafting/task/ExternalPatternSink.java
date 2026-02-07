package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;

import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

/**
 * Gives the ability to a {@link Task} to dump inputs of an external {@link Pattern} into the external target.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface ExternalPatternSink {
    /**
     * Accepts the given resources into the external target.
     * All resources MUST be able to be inserted for this method to return {@link Result#ACCEPTED},
     * otherwise, it must return {@link Result#REJECTED}.
     * If the sink is locked, it must return {@link Result#LOCKED}.
     * If the resources are not applicable for this sink, it must return {@link Result#SKIPPED}.
     *
     * @param pattern   the pattern
     * @param resources the resources
     * @param action    the action
     * @return the result
     */
    Result accept(Pattern pattern, Collection<ResourceAmount> resources, Action action);

    /**
     * @return the key for this sink
     */
    @Nullable
    default ExternalPatternSinkKey getKey() {
        return null;
    }

    enum Result {
        ACCEPTED,
        REJECTED,
        SKIPPED,
        LOCKED
    }
}
