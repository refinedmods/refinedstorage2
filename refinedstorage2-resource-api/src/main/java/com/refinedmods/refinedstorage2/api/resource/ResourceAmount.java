package com.refinedmods.refinedstorage2.api.resource;

import com.google.common.base.Preconditions;

public final class ResourceAmount<T> {
    private final T resource;
    private long amount;

    public static <T> void validate(T resource, long amount) {
        Preconditions.checkArgument(amount > 0, "Amount must be larger than 0");
        Preconditions.checkNotNull(resource, "Resource must not be null");
    }

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

    public void increment(long amount) {
        Preconditions.checkArgument(amount > 0, "Amount to increment must be larger than 0");
        this.amount += amount;
    }

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
