package com.refinedmods.refinedstorage2.core.list.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.StackListResult;

public class ItemStackList<ID> implements StackList<Rs2ItemStack> {
    private final Map<ID, Rs2ItemStack> entries = new HashMap<>();
    private final BiMap<UUID, Rs2ItemStack> index = HashBiMap.create();
    private final Function<Rs2ItemStack, ID> idFactory;

    public static ItemStackList<Rs2ItemStackIdentifier> create() {
        return new ItemStackList<>(Rs2ItemStackIdentifier::new);
    }

    public ItemStackList(Function<Rs2ItemStack, ID> idFactory) {
        this.idFactory = idFactory;
    }

    @Override
    public StackListResult<Rs2ItemStack> add(Rs2ItemStack template, long amount) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        ID entry = idFactory.apply(template);

        Rs2ItemStack existing = entries.get(entry);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(entry, template, amount);
        }
    }

    private StackListResult<Rs2ItemStack> addToExisting(Rs2ItemStack stack, long amount) {
        stack.increment(amount);

        return new StackListResult<>(stack, amount, index.inverse().get(stack), true);
    }

    private StackListResult<Rs2ItemStack> addNew(ID entry, Rs2ItemStack template, long amount) {
        Rs2ItemStack stack = template.copy();
        stack.setAmount(amount);

        UUID id = UUID.randomUUID();

        index.put(id, stack);
        entries.put(entry, stack);

        return new StackListResult<>(stack, amount, id, true);
    }

    @Override
    public Optional<StackListResult<Rs2ItemStack>> remove(Rs2ItemStack template, long amount) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        ID entry = idFactory.apply(template);

        Rs2ItemStack existing = entries.get(entry);
        if (existing != null) {
            UUID id = index.inverse().get(existing);

            if (existing.getAmount() - amount <= 0) {
                return removeCompletely(entry, existing, id);
            } else {
                return removePartly(amount, existing, id);
            }
        }

        return Optional.empty();
    }

    private Optional<StackListResult<Rs2ItemStack>> removePartly(long amount, Rs2ItemStack stack, UUID id) {
        stack.decrement(amount);

        return Optional.of(new StackListResult<>(stack, -amount, id, true));
    }

    private Optional<StackListResult<Rs2ItemStack>> removeCompletely(ID entry, Rs2ItemStack stack, UUID id) {
        index.remove(id);
        entries.remove(entry);

        return Optional.of(new StackListResult<>(stack, -stack.getAmount(), id, false));
    }

    @Override
    public Optional<Rs2ItemStack> get(Rs2ItemStack template) {
        return Optional.ofNullable(entries.get(idFactory.apply(template)));
    }

    @Override
    public Optional<Rs2ItemStack> get(UUID id) {
        return Optional.ofNullable(index.get(id));
    }

    @Override
    public Collection<Rs2ItemStack> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        index.clear();
        entries.clear();
    }
}
