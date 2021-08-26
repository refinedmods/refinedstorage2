package com.refinedmods.refinedstorage2.api.stack.test;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;

public record ItemStub(int id, String name, int maxAmount, String identifier) implements Rs2Item {
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
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return name;
    }
}
