package com.refinedmods.refinedstorage2.core.stack;

public interface Rs2Stack {
    boolean isEmpty();

    void increment(long amount);

    void decrement(long amount);

    void setAmount(long amount);

    long getAmount();

    Rs2Stack copy();
}
