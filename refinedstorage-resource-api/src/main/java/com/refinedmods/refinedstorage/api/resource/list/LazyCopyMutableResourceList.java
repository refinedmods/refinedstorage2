package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * An implementation of a {@link ResourceList} that lazily copies from an original list.
 * Only the entries that are changed are copied.
 */
@API(status = API.Status.STABLE, since = "2.0.0-beta.11")
public class LazyCopyMutableResourceList implements MutableResourceList {
    private final MutableResourceList original;
    private final MutableResourceList updates;
    private final Set<ResourceKey> pulled;

    private LazyCopyMutableResourceList(final MutableResourceList original, final MutableResourceList updates,
                                        final Set<ResourceKey> pulled) {
        this.original = original;
        this.updates = updates;
        this.pulled = pulled;
    }

    public static LazyCopyMutableResourceList create(final MutableResourceList original) {
        return new LazyCopyMutableResourceList(original, MutableResourceListImpl.create(), new HashSet<>());
    }

    @Override
    public OperationResult add(final ResourceKey resource, final long amount) {
        ResourceAmount.validate(resource, amount);
        pullIntoUpdates(resource);
        return updates.add(resource, amount);
    }

    private void pullIntoUpdates(final ResourceKey resource) {
        if (!pulled.add(resource)) {
            return;
        }
        final long amount = original.get(resource);
        if (amount > 0) {
            updates.add(resource, amount);
        }
    }

    @Nullable
    @Override
    public OperationResult remove(final ResourceKey resource, final long amount) {
        ResourceAmount.validate(resource, amount);
        pullIntoUpdates(resource);
        return updates.remove(resource, amount);
    }

    @Override
    public void clear() {
        pulled.addAll(original.getAll());
        updates.clear();
    }

    @Override
    public MutableResourceList copy() {
        return new LazyCopyMutableResourceList(original, updates.copy(), new HashSet<>(pulled));
    }

    @Override
    public Collection<ResourceAmount> copyState() {
        final Set<ResourceAmount> copy = new HashSet<>(original.copyState());
        copy.removeIf(ra -> pulled.contains(ra.resource()));
        copy.addAll(updates.copyState());
        return Collections.unmodifiableSet(copy);
    }

    @Override
    public Set<ResourceKey> getAll() {
        final Set<ResourceKey> all = new HashSet<>(original.getAll());
        all.removeAll(pulled);
        all.addAll(updates.getAll());
        return Collections.unmodifiableSet(all);
    }

    @Override
    public long get(final ResourceKey resource) {
        if (pulled.contains(resource)) {
            return updates.get(resource);
        }
        return original.get(resource);
    }

    @Override
    public boolean contains(final ResourceKey resource) {
        if (pulled.contains(resource)) {
            return updates.contains(resource);
        }
        return original.contains(resource);
    }

    @Override
    public boolean isEmpty() {
        return getAll().isEmpty();
    }

    @Override
    public String toString() {
        return original + " -> " + updates;
    }
}
