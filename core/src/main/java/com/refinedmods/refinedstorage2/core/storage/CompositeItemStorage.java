package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class CompositeItemStorage implements Storage<ItemStack> {
    private final List<Storage<ItemStack>> sources;
    private final StackList<ItemStack> list = new ItemStackList();

    public CompositeItemStorage(List<Storage<ItemStack>> sources) {
        this.sources = sources;
        fillListFromSources();
    }

    private void fillListFromSources() {
        sources.forEach(source -> source.getStacks().getAll().forEach(stack -> list.add(stack, stack.getCount())));
    }

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        int remaining = amount;
        for (Storage<ItemStack> source : sources) {
            Optional<ItemStack> stack = source.extract(template, remaining, action);
            if (stack.isPresent()) {
                remaining -= stack.get().getCount();
                if (remaining == 0) {
                    break;
                }
            }
        }

        int extracted = amount - remaining;
        if (action == Action.EXECUTE && extracted > 0) {
            list.remove(template, extracted);
        }

        if (extracted == 0) {
            return Optional.empty();
        } else {
            ItemStack result = template.copy();
            result.setCount(extracted);
            return Optional.of(result);
        }
    }

    @Override
    public Optional<ItemStack> insert(ItemStack template, int amount, Action action) {
        int remainder = amount;
        for (Storage<ItemStack> source : sources) {
            Optional<ItemStack> remainderStack = source.insert(template, remainder, action);
            if (!remainderStack.isPresent()) {
                remainder = 0;
                break;
            }
            remainder = remainderStack.get().getCount();
        }

        if (action == Action.EXECUTE) {
            int inserted = amount - remainder;
            if (inserted > 0) {
                list.add(template, inserted);
            }
        }

        if (remainder == 0) {
            return Optional.empty();
        } else {
            ItemStack remainderStack = template.copy();
            remainderStack.setCount(remainder);
            return Optional.of(remainderStack);
        }
    }

    @Override
    public StackList<ItemStack> getStacks() {
        return list;
    }

    @Override
    public int getStored() {
        return sources.stream().mapToInt(Storage::getStored).sum();
    }
}
