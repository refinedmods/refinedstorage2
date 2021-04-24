package com.refinedmods.refinedstorage2.core.item;

public class ItemStub implements Rs2Item {
    private final int id;
    private final String name;
    private final int maxAmount;

    public ItemStub(int id, String name, int maxAmount) {
        this.id = id;
        this.name = name;
        this.maxAmount = maxAmount;
    }

    @Override
    public int getMaxAmount() {
        return maxAmount;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
