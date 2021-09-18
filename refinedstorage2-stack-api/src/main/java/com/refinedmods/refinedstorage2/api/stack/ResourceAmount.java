package com.refinedmods.refinedstorage2.api.stack;

import com.google.common.base.Preconditions;

public class ResourceAmount<T> {
    private final T resource;
    private long amount;

    public ResourceAmount(T resource, long amount) {
        this.resource = Preconditions.checkNotNull(resource);
        this.amount = amount;
    }

    public T getResource() {
        return resource;
    }

    public long getAmount() {
        return amount;
    }

    public void increment(long amount) {
        this.amount += amount;
    }

    public void decrement(long amount) {
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
