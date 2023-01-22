package com.refinedmods.refinedstorage2.api.grid.view;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Map;
import java.util.Set;

public class FakeGridResource extends AbstractGridResource {
    public FakeGridResource(final String name, final long amount) {
        this(new ResourceAmount<>(name, amount));
    }

    public FakeGridResource(final String name) {
        this(new ResourceAmount<>(name, 1));
    }

    public FakeGridResource(final ResourceAmount<?> resourceAmount) {
        super(
            resourceAmount,
            (String) resourceAmount.getResource(),
            Map.of(
                FakeGridResourceAttributeKeys.MOD_ID, Set.of((String) resourceAmount.getResource()),
                FakeGridResourceAttributeKeys.MOD_NAME, Set.of((String) resourceAmount.getResource())
            )
        );
    }

    public FakeGridResource(final String name,
                            final long amount,
                            final String modId,
                            final String modName,
                            final Set<String> tags) {
        super(
            new ResourceAmount<>(name, amount),
            name,
            Map.of(
                FakeGridResourceAttributeKeys.MOD_ID, Set.of(modId),
                FakeGridResourceAttributeKeys.MOD_NAME, Set.of(modName),
                FakeGridResourceAttributeKeys.TAGS, tags
            )
        );
    }

    public FakeGridResource zeroed() {
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
