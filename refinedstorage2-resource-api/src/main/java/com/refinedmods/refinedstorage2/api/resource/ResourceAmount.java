package com.refinedmods.refinedstorage2.api.resource;

import com.google.common.base.Preconditions;
import org.apiguardian.api.API;

/**
 * A class representing a resource of an arbitrary type, and a corresponding amount.
 * The resource cannot be mutated but the amount can be modified.
 *
 * @param <T> the type of resource
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public final class ResourceAmount<T> {
    private final T resource;
    private long amount;

    public static <T> void validate(T resource, long amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be larger than 0");
        Preconditions.checkNotNull(resource, "Resource must not be null");
    }

    /**
     * @param resource the resource, must be non-null
     * @param amount   the amount, must be larger than 0
     */
    public ResourceAmount(T resource, long amount) {
        validate(resource, amount);
        this.resource = resource;
        this.amount = amount;
    }

    public T getResource() {
        return resource;
    }

    public long getAmount() {
        return amount;
    }

    /**
     * Increments with the given amount.
     *
     * @param amount the amount to increment, must be larger than 0
     */
    public void increment(long amount) {
        Preconditions.checkArgument(amount > 0, "Amount to increment must be larger than 0");
        this.amount += amount;
    }

    /**
     * Decrements with the given amount.
     * The amount, after performing this decrement, may not be 0 or less than 0.
     *
     * @param amount the amount to decrement, a positive number
     */
    public void decrement(long amount) {
        Preconditions.checkArgument(amount > 0, "Amount to decrement must be larger than 0");
        Preconditions.checkArgument((this.amount - amount) > 0, "Cannot decrement more than " + (amount - 1));
        this.amount -= amount;
    }

    @Override
    public String toString() {
        return "ResourceAmount{" +
                "resource=" + resource +
                ", amount=" + amount +
                '}';
    }
}
