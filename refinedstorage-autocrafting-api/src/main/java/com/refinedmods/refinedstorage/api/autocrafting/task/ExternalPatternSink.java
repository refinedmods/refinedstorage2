package com.refinedmods.refinedstorage.api.autocrafting.task;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;

import java.util.Collection;

import org.apiguardian.api.API;

/**
 * Gives the ability to a {@link Task} to dump inputs of an external {@link Pattern} into the external target.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.4.12")
public interface ExternalPatternSink {
    /**
     * Inserts the given resources into the external target.
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
    Result insertAll(Pattern pattern, Collection<ResourceAmount> resources, Action action);

    /**
     * Returns a unique key for this sink. This is used to unlock sinks that became locked after inserting an
     * external iteration.
     * The object identity of {@link ExternalPatternSinkKey} must remain stable across the lifecycle of the
     * {@link ExternalPatternSink}.
     *
     * @return the key for this sink
     */
    ExternalPatternSinkKey getKey();

    /**
     * If this sink acts as a proxy for another sink, the key of the other sink should be returned here.
     * This is used so that the correct sink can be notified (and potentially be unlocked)
     * when an external iteration is received.
     *
     * @return the key of the sink this sink is proxying for, or the same key as {@link #getKey()} if it is not a proxy
     */
    default ExternalPatternSinkKey unwrapKey(final Pattern pattern) {
        return getKey();
    }

    enum Result {
        ACCEPTED,
        REJECTED,
        SKIPPED,
        LOCKED
    }
}
