package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CompositeItemStorage implements Storage<Rs2ItemStack> {
    private final List<Storage<Rs2ItemStack>> sources;
    private final StackList<Rs2ItemStack> list;

    public CompositeItemStorage(List<Storage<Rs2ItemStack>> sources, StackList<Rs2ItemStack> list) {
        this.sources = sources;
        this.list = list;

        fillListFromSources();
        sortSources();
    }

    public static CompositeItemStorage empty() {
        return new CompositeItemStorage(Collections.emptyList(), ItemStackList.create());
    }

    public void sortSources() {
        sources.sort(PrioritizedStorageComparator.INSTANCE);
    }

    private void fillListFromSources() {
        sources.forEach(source -> source.getStacks().forEach(stack -> list.add(stack, stack.getAmount())));
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
        long extracted = extractFromStorages(template, amount, action);
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(template, extracted);
        }

        if (extracted == 0) {
            return Optional.empty();
        } else {
            Rs2ItemStack result = template.copy();
            result.setAmount(extracted);
            return Optional.of(result);
        }
    }

    private long extractFromStorages(Rs2ItemStack template, long amount, Action action) {
        long remaining = amount;
        for (Storage<Rs2ItemStack> source : sources) {
            Optional<Rs2ItemStack> stack = source.extract(template, remaining, action);
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
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
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
            Rs2ItemStack remainderStack = template.copy();
            remainderStack.setAmount(remainder);
            return Optional.of(remainderStack);
        }
    }

    private long insertIntoStorages(Rs2ItemStack template, long amount, Action action) {
        long remainder = amount;
        for (Storage<Rs2ItemStack> source : sources) {
            Optional<Rs2ItemStack> remainderStack = source.insert(template, remainder, action);
            if (!remainderStack.isPresent()) {
                remainder = 0;
                break;
            }
            remainder = remainderStack.get().getAmount();
        }
        return remainder;
    }

    @Override
    public Collection<Rs2ItemStack> getStacks() {
        return list.getAll();
    }

    @Override
    public long getStored() {
        return sources.stream().mapToLong(Storage::getStored).sum();
    }
}
