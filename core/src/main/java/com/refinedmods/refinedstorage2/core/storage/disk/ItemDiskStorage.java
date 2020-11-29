package com.refinedmods.refinedstorage2.core.storage.disk;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ItemDiskStorage implements StorageDisk<ItemStack> {
    private final StackList<ItemStack> list = new ItemStackList();
    private final int capacity;
    private int stored;

    public ItemDiskStorage(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        return list.get(template).map(stack -> {
            if (amount > stack.getCount()) {
                return extractCompletely(stack, action);
            } else {
                return extractPartly(stack, amount, action);
            }
        });
    }

    private ItemStack extractPartly(ItemStack stack, int amount, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(stack, amount);
            stored -= amount;
        }

        ItemStack extracted = stack.copy();
        extracted.setCount(amount);
        return extracted;
    }

    private ItemStack extractCompletely(ItemStack stack, Action action) {
        if (action == Action.EXECUTE) {
            list.remove(stack, stack.getCount());
            stored -= stack.getCount();
        }

        return stack;
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        if (stored + amount > capacity) {
            return insertPartly(template, capacity - stored, amount - (capacity - stored), action);
        } else {
            return insertCompletely(template, amount, action);
        }
    }

    @Override
    public StackList<ItemStack> getStacks() {
        return list;
    }

    private Optional<ItemStack> insertPartly(ItemStack template, int amount, int remainder, Action action) {
        if (action == Action.EXECUTE && amount > 0) {
            stored += amount;
            list.add(template, amount);
        }

        ItemStack remainderStack = template.copy();
        remainderStack.setCount(remainder);
        return Optional.of(remainderStack);
    }

    @NotNull
    private Optional<ItemStack> insertCompletely(ItemStack template, int amount, Action action) {
        if (action == Action.EXECUTE) {
            stored += amount;
            list.add(template, amount);
        }

        return Optional.empty();
    }

    @Override
    public int getStored() {
        return stored;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }
}
