package com.refinedmods.refinedstorage2.core.item;

public final class Rs2ItemStack {
    public static final Rs2ItemStack EMPTY = new Rs2ItemStack(null, 0, null);

    private Rs2Item item;
    private long amount;
    private Object tag;
    private boolean empty;

    public Rs2ItemStack(Rs2Item item) {
        this(item, 1);
    }

    public Rs2ItemStack(Rs2Item item, long amount) {
        this(item, amount, null);
    }

    public Rs2ItemStack(Rs2Item item, long amount, Object tag) {
        this.item = item;
        this.amount = amount;
        this.tag = tag;
        this.updateEmptyState();
    }

    public Rs2Item getItem() {
        return item;
    }

    public void setItem(Rs2Item item) {
        this.item = item;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
        this.updateEmptyState();
    }

    public void increment(long amount) {
        setAmount(this.amount + amount);
    }

    public void decrement(long amount) {
        setAmount(this.amount - amount);
    }

    public Rs2ItemStack copy() {
        return new Rs2ItemStack(item, amount, tag);
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

    public boolean isEmpty() {
        return empty;
    }

    public int getMaxCount() {
        return item.getMaxAmount();
    }

    public String getName() {
        return item.getName();
    }

    @Override
    public String toString() {
        return "Rs2ItemStack{" +
                "item=" + item +
                ", amount=" + amount +
                ", tag=" + tag +
                ", empty=" + empty +
                '}';
    }
}
