package com.refinedmods.refinedstorage2.api.stack.fluid;

import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;

// TODO: Remove eventually?
public final class Rs2FluidStack implements Rs2Stack {
    public static final Rs2FluidStack EMPTY = new Rs2FluidStack(null, 0, null);

    private final Rs2Fluid fluid;
    private long amount;
    private Object tag;
    private boolean empty;

    public Rs2FluidStack(Rs2Fluid fluid) {
        this(fluid, 1);
    }

    public Rs2FluidStack(Rs2Fluid fluid, long amount) {
        this(fluid, amount, null);
    }

    public Rs2FluidStack(Rs2Fluid fluid, long amount, Object tag) {
        this.fluid = fluid;
        this.amount = amount;
        this.tag = tag;
        this.updateEmptyState();
    }

    public Rs2Fluid getFluid() {
        return fluid;
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public void setAmount(long amount) {
        this.amount = amount;
        this.updateEmptyState();
    }

    @Override
    public void increment(long amount) {
        setAmount(this.amount + amount);
    }

    @Override
    public void decrement(long amount) {
        setAmount(this.amount - amount);
    }

    @Override
    public Rs2FluidStack copy() {
        if (isEmpty()) {
            return EMPTY;
        }
        return new Rs2FluidStack(fluid, amount, tag);
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    private void updateEmptyState() {
        this.empty = this.amount <= 0;
    }

    @Override
    public boolean isEmpty() {
        return empty;
    }

    @Override
    public String toString() {
        return "Rs2FluidStack{" +
                "fluid=" + fluid +
                ", amount=" + amount +
                ", tag=" + tag +
                ", empty=" + empty +
                '}';
    }
}
