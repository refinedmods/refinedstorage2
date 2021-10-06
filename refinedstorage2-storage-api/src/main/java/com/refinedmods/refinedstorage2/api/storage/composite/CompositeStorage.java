package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceList;
import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Collection;
import java.util.List;

public class CompositeStorage<T> implements Storage<T> {
    private final List<Storage<T>> sources;
    private final ResourceList<T> list;

    public CompositeStorage(List<Storage<T>> sources, ResourceList<T> list) {
        this.sources = sources;
        this.list = list;

        fillListFromSources();
        sortSources();
    }

    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    private void fillListFromSources() {
        sources.forEach(source -> source.getAll().forEach(list::add));
    }

    @Override
    public long extract(T resource, long amount, Action action) {
        long extracted = extractFromStorages(resource, amount, action);
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(resource, extracted);
        }
        return extracted;
    }

    private long extractFromStorages(T template, long amount, Action action) {
        long remaining = amount;
        for (Storage<T> source : sources) {
            long extracted = source.extract(template, remaining, action);
            remaining -= extracted;
            if (remaining == 0) {
                break;
            }
        }

        return amount - remaining;
    }

    @Override
    public long insert(T resource, long amount, Action action) {
        long remainder = insertIntoStorages(resource, amount, action);

        if (action == Action.EXECUTE) {
            long inserted = amount - remainder;
            if (inserted > 0) {
                list.add(resource, inserted);
            }
        }

        return remainder;
    }

    private long insertIntoStorages(T template, long amount, Action action) {
        long remainder = amount;
        for (Storage<T> source : sources) {
            remainder = source.insert(template, remainder, action);
            if (remainder == 0) {
                break;
            }
        }
        return remainder;
    }

    @Override
    public Collection<ResourceAmount<T>> getAll() {
        return list.getAll();
    }

    @Override
    public long getStored() {
        return sources.stream().mapToLong(Storage::getStored).sum();
    }
}
