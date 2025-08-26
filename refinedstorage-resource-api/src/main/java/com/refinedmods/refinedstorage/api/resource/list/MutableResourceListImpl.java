package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.core.CoreValidations;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * An implementation of a {@link ResourceList} that stores the resource entries in memory.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class MutableResourceListImpl implements MutableResourceList {
    private final Map<ResourceKey, Entry> entries;

    private MutableResourceListImpl(final Map<ResourceKey, Entry> entries) {
        this.entries = entries;
    }

    public static MutableResourceListImpl create() {
        return new MutableResourceListImpl(new HashMap<>());
    }

    private static MutableResourceListImpl createCopy(final Map<ResourceKey, Entry> entries) {
        final Map<ResourceKey, Entry> newEntries = HashMap.newHashMap(entries.size());
        entries.forEach((key, entry) -> newEntries.put(key, new Entry(key, entry.amount)));
        return new MutableResourceListImpl(newEntries);
    }

    public static MutableResourceListImpl orderPreserving() {
        return new MutableResourceListImpl(new LinkedHashMap<>());
    }

    @Override
    public OperationResult add(final ResourceKey resource, final long amount) {
        ResourceAmount.validate(resource, amount);
        final Entry existing = entries.get(resource);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(resource, amount);
        }
    }

    private OperationResult addToExisting(final Entry entry, final long amount) {
        entry.increment(amount);
        return new OperationResult(entry.resource, entry.amount, amount, true);
    }

    private OperationResult addNew(final ResourceKey resource, final long amount) {
        final Entry entry = new Entry(resource, amount);
        entries.put(resource, entry);
        return new OperationResult(resource, amount, amount, true);
    }

    @Override
    @Nullable
    public OperationResult remove(final ResourceKey resource, final long amount) {
        ResourceAmount.validate(resource, amount);
        final Entry existing = entries.get(resource);
        if (existing != null) {
            if (existing.amount - amount <= 0) {
                return removeCompletely(existing);
            } else {
                return removePartly(amount, existing);
            }
        }
        return null;
    }

    private OperationResult removePartly(final long amount, final Entry entry) {
        entry.decrement(amount);
        return new OperationResult(entry.resource, entry.amount, -amount, true);
    }

    private OperationResult removeCompletely(final Entry entry) {
        entries.remove(entry.resource);
        return new OperationResult(
            entry.resource,
            0,
            -entry.amount,
            false
        );
    }

    @Override
    public Collection<ResourceAmount> copyState() {
        return entries.values().stream().map(Entry::toResourceAmount).toList();
    }

    @Override
    public Set<ResourceKey> getAll() {
        return entries.keySet();
    }

    @Override
    public long get(final ResourceKey resource) {
        final Entry entry = entries.get(resource);
        return entry != null ? entry.amount : 0;
    }

    @Override
    public boolean contains(final ResourceKey resource) {
        return entries.containsKey(resource);
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public void clear() {
        entries.clear();
    }

    @Override
    public MutableResourceList copy() {
        return MutableResourceListImpl.createCopy(entries);
    }

    @Override
    public String toString() {
        return entries.toString();
    }

    private static class Entry {
        private final ResourceKey resource;
        private long amount;

        private Entry(final ResourceKey resource, final long amount) {
            this.resource = resource;
            this.amount = amount;
        }

        private void increment(final long amountToIncrement) {
            CoreValidations.validateLargerThanZero(amountToIncrement, "Amount to increment must be larger than 0");
            this.amount += amountToIncrement;
        }

        private void decrement(final long amountToDecrement) {
            CoreValidations.validateLargerThanZero(amountToDecrement, "Amount to decrement must be larger than 0");
            CoreValidations.validateLargerThanZero(
                amount - amountToDecrement,
                "Cannot decrement, amount will be zero or negative"
            );
            this.amount -= amountToDecrement;
        }

        private ResourceAmount toResourceAmount() {
            return new ResourceAmount(resource, amount);
        }

        @Override
        public String toString() {
            return String.valueOf(amount);
        }
    }
}
