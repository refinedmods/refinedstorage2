package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.grid.view.stack.GridStack;
import com.refinedmods.refinedstorage2.api.stack.ResourceAmount;

import java.util.Set;

public class FakeGridStack extends GridStack<String> {
    public FakeGridStack(String name, long amount) {
        this(new ResourceAmount<>(name, amount));
    }

    public FakeGridStack(ResourceAmount<String> resource) {
        super(
                resource,
                resource.getResource(),
                resource.getResource(),
                resource.getResource(),
                Set.of()
        );
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
