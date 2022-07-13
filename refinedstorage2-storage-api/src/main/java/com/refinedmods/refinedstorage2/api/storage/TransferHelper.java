package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.core.CoreValidations;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.1")
public final class TransferHelper {
    private TransferHelper() {
    }

    /**
     * Transfers a given resource and amount from source to destination.
     * If there is not enough stored of the given resource in the source, it will only try to extract what's there.
     * If there is not enough space in the destination, it will only insert for the space that's there.
     *
     * @param resource    the resource
     * @param amount      the amount to transfer
     * @param actor       the actor performing the transfer
     * @param source      the source to extract from
     * @param destination the destination to insert to
     * @param <T>         the resource type
     * @return the amount transferred
     */
    public static <T> long transfer(final T resource,
                                    final long amount,
                                    final Actor actor,
                                    final ExtractableStorage<T> source,
                                    final InsertableStorage<T> destination) {
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
        CoreValidations.validateEquals(
            extracted,
            inserted,
            "Destination storage did not accept resource from source storage, even after simulating"
        );
        return inserted;
    }
}
