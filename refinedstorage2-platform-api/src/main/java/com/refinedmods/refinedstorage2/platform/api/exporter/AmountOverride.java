package com.refinedmods.refinedstorage2.platform.api.exporter;

import java.util.function.LongSupplier;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.12")
@FunctionalInterface
public interface AmountOverride {
    AmountOverride NONE = new AmountOverride() {
        @Override
        public <T> long overrideAmount(final T resource, final long amount, final LongSupplier currentAmount) {
            return amount;
        }
    };

    /**
     * Modifies the requested amount to a new amount.
     *
     * @param resource      the resource
     * @param amount        the original requested amount
     * @param currentAmount the current amount present in the source
     * @param <T>           the resource type
     * @return the new requested amount, may be 0
     */
    <T> long overrideAmount(T resource, long amount, LongSupplier currentAmount);
}
