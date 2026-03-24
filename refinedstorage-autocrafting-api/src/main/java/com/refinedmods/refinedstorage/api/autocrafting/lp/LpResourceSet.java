package com.refinedmods.refinedstorage.api.autocrafting.lp;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mutable resource-count map used by the standalone LP crafting prototype.
 */
public class LpResourceSet {
    private final Map<ResourceKey, Long> amounts;

    public LpResourceSet() {
        this.amounts = new LinkedHashMap<>();
    }

    public LpResourceSet(final Map<ResourceKey, Long> amounts) {
        this();
        Objects.requireNonNull(amounts, "amounts cannot be null");
        amounts.forEach(this::setAmount);
    }

    public static LpResourceSet empty() {
        return new LpResourceSet();
    }

    public static LpResourceSet copyOf(final LpResourceSet other) {
        Objects.requireNonNull(other, "other cannot be null");
        return new LpResourceSet(other.amounts);
    }

    public static LpResourceSet fromResourceAmounts(final Collection<ResourceAmount> resourceAmounts) {
        Objects.requireNonNull(resourceAmounts, "resourceAmounts cannot be null");
        final LpResourceSet result = new LpResourceSet();
        for (final ResourceAmount resourceAmount : resourceAmounts) {
            result.addAmount(resourceAmount.resource(), resourceAmount.amount());
        }
        return result;
    }

    public Map<ResourceKey, Long> asMap() {
        return Collections.unmodifiableMap(amounts);
    }

    public Set<ResourceKey> resourceKeys() {
        return Collections.unmodifiableSet(amounts.keySet());
    }

    public long getAmount(final ResourceKey resource) {
        Objects.requireNonNull(resource, "resource cannot be null");
        return amounts.getOrDefault(resource, 0L);
    }

    public long totalAmount() {
        long total = 0;
        for (final long amount : amounts.values()) {
            total += amount;
        }
        return total;
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    public void setAmount(final ResourceKey resource, final long amount) {
        Objects.requireNonNull(resource, "resource cannot be null");
        if (amount == 0L) {
            amounts.remove(resource);
            return;
        }
        amounts.put(resource, amount);
    }

    public void addAmount(final ResourceKey resource, final long amount) {
        Objects.requireNonNull(resource, "resource cannot be null");
        if (amount == 0L) {
            return;
        }
        setAmount(resource, getAmount(resource) + amount);
    }

    public void addAll(final LpResourceSet other) {
        Objects.requireNonNull(other, "other cannot be null");
        other.amounts.forEach(this::addAmount);
    }

    public void subtractAmount(final ResourceKey resource, final long amount) {
        addAmount(resource, -amount);
    }

    public LpResourceSet copy() {
        return copyOf(this);
    }

    @Override
    public String toString() {
        return amounts.toString();
    }
}
