package com.refinedmods.refinedstorage2.core.storage.disk;

import java.util.Collection;
import java.util.Optional;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;

public class ItemDiskStorage implements StorageDisk<Rs2ItemStack> {
    private final StackList<Rs2ItemStack> list = ItemStackList.create();
    private final long capacity;
    private long stored;

    public ItemDiskStorage(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public Optional<Rs2ItemStack> extract(Rs2ItemStack template, long amount, Action action) {
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

    private Rs2ItemStack extractPartly(Rs2ItemStack stack, long amount, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(stack, amount);
            stored -= amount;
        }

        Rs2ItemStack extracted = stack.copy();
        extracted.setAmount(amount);
        return extracted;
    }

    private Rs2ItemStack extractCompletely(Rs2ItemStack stack, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(stack, stack.getAmount());
            stored -= stack.getAmount();
        }

        return stack;
    }

    @Override
    public Optional<Rs2ItemStack> insert(Rs2ItemStack template, long amount, Action action) {
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
    public Collection<Rs2ItemStack> getStacks() {
        return list.getAll();
    }

    private Optional<Rs2ItemStack> insertPartly(Rs2ItemStack template, long amount, long remainder, Action action) {
        if (action == Action.EXECUTE && amount > 0) {
            stored += amount;
            list.add(template, amount);
        }

        Rs2ItemStack remainderStack = template.copy();
        remainderStack.setAmount(remainder);
        return Optional.of(remainderStack);
    }

    private Optional<Rs2ItemStack> insertCompletely(Rs2ItemStack template, long amount, Action action) {
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
