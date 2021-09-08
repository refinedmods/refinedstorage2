package com.refinedmods.refinedstorage2.api.storage.disk;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.list.StackList;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;

import java.util.Collection;
import java.util.Optional;

public class StorageDiskImpl<T extends Rs2Stack> implements StorageDisk<T> {
    private final StackList<T> list;
    private final long capacity;
    private long stored;

    public StorageDiskImpl(long capacity, StackList<T> list) {
        this.capacity = capacity;
        this.list = list;
    }

    public static StorageDisk<Rs2ItemStack> createItemStorageDisk(long capacity) {
        return new StorageDiskImpl<>(capacity, StackListImpl.createItemStackList());
    }

    public static StorageDisk<Rs2FluidStack> createFluidStorageDisk(long capacity) {
        return new StorageDiskImpl<>(capacity, StackListImpl.createFluidStackList());
    }

    @Override
    public Optional<T> extract(T template, long amount, Action action) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        return list.get(template).map(stack -> {
            if (amount > stack.getAmount()) {
                return extractCompletely(stack, action);
            } else {
                return extractPartly(stack, amount, action);
            }
        });
    }

    private T extractPartly(T stack, long amount, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(stack, amount);
            stored -= amount;
        }

        T extracted = (T) stack.copy();
        extracted.setAmount(amount);
        return extracted;
    }

    private T extractCompletely(T stack, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(stack, stack.getAmount());
            stored -= stack.getAmount();
        }

        return stack;
    }

    @Override
    public Optional<T> insert(T template, long amount, Action action) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        if (capacity >= 0 && stored + amount > capacity) {
            return insertPartly(template, capacity - stored, amount - (capacity - stored), action);
        } else {
            return insertCompletely(template, amount, action);
        }
    }

    @Override
    public Collection<T> getStacks() {
        return list.getAll();
    }

    private Optional<T> insertPartly(T template, long amount, long remainder, Action action) {
        if (action == Action.EXECUTE && amount > 0) {
            stored += amount;
            list.add(template, amount);
        }

        T remainderStack = (T) template.copy();
        remainderStack.setAmount(remainder);
        return Optional.of(remainderStack);
    }

    private Optional<T> insertCompletely(T template, long amount, Action action) {
        if (action == Action.EXECUTE) {
            stored += amount;
            list.add(template, amount);
        }

        return Optional.empty();
    }

    @Override
    public long getStored() {
        return stored;
    }

    @Override
    public long getCapacity() {
        return capacity;
    }
}
