package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Set;

public class FakeGridStack extends GridStack<String> {
    public FakeGridStack(String name, long amount) {
        this(new ResourceAmount<>(name, amount));
    }

    public FakeGridStack(String name) {
        this(new ResourceAmount<>(name, 1));
    }

    public FakeGridStack(ResourceAmount<String> resourceAmount) {
        super(
                resourceAmount,
                resourceAmount.getResource(),
                resourceAmount.getResource(),
                resourceAmount.getResource(),
                Set.of()
        );
    }

    public FakeGridStack(String name, long amount, String modId, String modName, Set<String> tags) {
        super(new ResourceAmount<>(name, amount), name, modId, modName, tags);
    }

    public FakeGridStack zeroed() {
        setZeroed(true);
        return this;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String toString() {
        return super.getResourceAmount().toString();
    }
}
