package com.refinedmods.refinedstorage2.core.list.item;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;
import net.minecraft.item.ItemStack;

import java.util.*;

public class ItemStackList implements StackList<ItemStack> {
    private final Map<ListEntry, ItemStack> entries = new HashMap<>();
    private final BiMap<UUID, ItemStack> index = HashBiMap.create();

    @Override
    public StackListResult<ItemStack> add(ItemStack template, int amount) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        ListEntry entry = new ListEntry(template);

        ItemStack existing = entries.get(entry);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(entry, template, amount);
        }
    }

    private StackListResult<ItemStack> addToExisting(ItemStack stack, int amount) {
        stack.increment(amount);

        return new StackListResult<>(stack, amount, index.inverse().get(stack));
    }

    private StackListResult<ItemStack> addNew(ListEntry entry, ItemStack template, int amount) {
        ItemStack stack = template.copy();
        stack.setCount(amount);

        UUID id = UUID.randomUUID();

        index.put(id, stack);
        entries.put(entry, stack);

        return new StackListResult<>(stack, amount, id);
    }

    @Override
    public Optional<StackListResult<ItemStack>> remove(ItemStack template, int amount) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        ListEntry entry = new ListEntry(template);

        ItemStack existing = entries.get(entry);
        if (existing != null) {
            UUID id = index.inverse().get(existing);

            if (existing.getCount() - amount <= 0) {
                return removeCompletely(entry, existing, id);
            } else {
                return removePartly(amount, existing, id);
            }
        }

        return Optional.empty();
    }

    private Optional<StackListResult<ItemStack>> removePartly(int amount, ItemStack stack, UUID id) {
        stack.decrement(amount);

        return Optional.of(new StackListResult<>(stack, -amount, id));
    }

    private Optional<StackListResult<ItemStack>> removeCompletely(ListEntry entry, ItemStack stack, UUID id) {
        index.remove(id);
        entries.remove(entry);

        return Optional.of(new StackListResult<>(stack, -stack.getCount(), id));
    }

    @Override
    public Optional<ItemStack> get(ItemStack template) {
        return Optional.ofNullable(entries.get(new ListEntry(template)));
    }

    @Override
    public Optional<ItemStack> get(UUID id) {
        return Optional.ofNullable(index.get(id));
    }

    @Override
    public Collection<ItemStack> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        index.clear();
        entries.clear();
    }
}
