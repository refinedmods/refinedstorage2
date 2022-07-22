package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import javax.annotation.Nullable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public final class TransferHelper {
    private static final Logger LOGGER = LogManager.getLogger();

    private TransferHelper() {
    }

    /**
     * Transfers a given resource and amount from source to destination.
     * If there is not enough stored of the given resource in the source, it will only try to extract what's there.
     * If there is not enough space in the destination, it will only insert for the space that's there.
     * If the last eventual insert into the destination after all these checks fails or only partly succeeds,
     * the fallback storage will be used to reinsert leftovers in.
     * If there's no fallback inventory given in that case, a {@link IllegalStateException} will be thrown.
     *
     * @param resource    the resource
     * @param amount      the amount to transfer
     * @param actor       the actor performing the transfer
     * @param source      the source to extract from
     * @param destination the destination to insert to
     * @param fallback    the fallback to insert leftovers in
     * @param <T>         the resource type
     * @return the amount transferred
     */
    public static <T> long transfer(final T resource,
                                    final long amount,
                                    final Actor actor,
                                    final ExtractableStorage<T> source,
                                    final InsertableStorage<T> destination,
                                    @Nullable final InsertableStorage<T> fallback) {
        ResourceAmount.validate(resource, amount);
        final long extractedSimulated = source.extract(resource, amount, Action.SIMULATE, actor);
        if (extractedSimulated == 0) {
            return 0;
        }
        final long insertedSimulated = destination.insert(resource, extractedSimulated, Action.SIMULATE, actor);
        if (insertedSimulated == 0) {
            return 0;
        }
        final long extracted = source.extract(resource, insertedSimulated, Action.EXECUTE, actor);
        if (extracted == 0) {
            return 0;
        }
        final long inserted = destination.insert(resource, extracted, Action.EXECUTE, actor);
        final long leftover = extracted - inserted;
        if (leftover > 0) {
            if (fallback != null) {
                handleLeftover(resource, actor, fallback, leftover);
            } else {
                throw new IllegalStateException(
                    "Destination storage did not accept resource from source storage, even after simulating"
                );
            }
        }
        return inserted;
    }

    private static <T> void handleLeftover(final T resource,
                                           final Actor actor,
                                           final InsertableStorage<T> fallback,
                                           final long leftover) {
        final long leftoverInserted = fallback.insert(resource, leftover, Action.EXECUTE, actor);
        final long leftoverNotInserted = leftover - leftoverInserted;
        if (leftoverNotInserted > 0) {
            LOGGER.warn(
                "Fallback didn't accept all leftovers, {} of {} has been voided",
                leftoverNotInserted,
                leftover
            );
        }
    }
}
