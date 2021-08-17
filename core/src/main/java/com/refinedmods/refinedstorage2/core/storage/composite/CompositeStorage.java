package com.refinedmods.refinedstorage2.core.storage.composite;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.StackListImpl;
import com.refinedmods.refinedstorage2.core.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.core.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.Storage;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CompositeStorage<S extends Rs2Stack> implements Storage<S> {
    private final List<Storage<S>> sources;
    private final StackList<S> list;

    public CompositeStorage(List<Storage<S>> sources, StackList<S> list) {
        this.sources = sources;
        this.list = list;

        fillListFromSources();
        sortSources();
    }

    public static CompositeStorage<Rs2ItemStack> emptyItemStackStorage() {
        return new CompositeStorage<>(Collections.emptyList(), StackListImpl.createItemStackList());
    }

    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    private void fillListFromSources() {
        sources.forEach(source -> source.getStacks().forEach(stack -> list.add(stack, stack.getAmount())));
    }

    @Override
    public Optional<S> extract(S template, long amount, Action action) {
        long extracted = extractFromStorages(template, amount, action);
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(template, extracted);
        }

        if (extracted == 0) {
            return Optional.empty();
        } else {
            S result = (S) template.copy();
            result.setAmount(extracted);
            return Optional.of(result);
        }
    }

    private long extractFromStorages(S template, long amount, Action action) {
        long remaining = amount;
        for (Storage<S> source : sources) {
            Optional<S> stack = source.extract(template, remaining, action);
            if (stack.isPresent()) {
                remaining -= stack.get().getAmount();
                if (remaining == 0) {
                    break;
                }
            }
        }

        return amount - remaining;
    }

    @Override
    public Optional<S> insert(S template, long amount, Action action) {
        long remainder = insertIntoStorages(template, amount, action);

        if (action == Action.EXECUTE) {
            long inserted = amount - remainder;
            if (inserted > 0) {
                list.add(template, inserted);
            }
        }

        if (remainder == 0) {
            return Optional.empty();
        } else {
            S remainderStack = (S) template.copy();
            remainderStack.setAmount(remainder);
            return Optional.of(remainderStack);
        }
    }

    private long insertIntoStorages(S template, long amount, Action action) {
        long remainder = amount;
        for (Storage<S> source : sources) {
            Optional<S> remainderStack = source.insert(template, remainder, action);
            if (!remainderStack.isPresent()) {
                remainder = 0;
                break;
            }
            remainder = remainderStack.get().getAmount();
        }
        return remainder;
    }

    @Override
    public Collection<S> getStacks() {
        return list.getAll();
    }

    @Override
    public long getStored() {
        return sources.stream().mapToLong(Storage::getStored).sum();
    }
}
