package com.refinedmods.refinedstorage2.api.stack.list;

import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class StackListImpl<I, S extends Rs2Stack> implements StackList<S> {
    private final Map<I, S> entries = new HashMap<>();
    private final BiMap<UUID, S> index = HashBiMap.create();
    private final Function<S, I> idFactory;

    public StackListImpl(Function<S, I> idFactory) {
        this.idFactory = idFactory;
    }

    public static StackListImpl<Rs2ItemStackIdentifier, Rs2ItemStack> createItemStackList() {
        return new StackListImpl<>(Rs2ItemStackIdentifier::new);
    }

    public static StackListImpl<Rs2FluidStackIdentifier, Rs2FluidStack> createFluidStackList() {
        return new StackListImpl<>(Rs2FluidStackIdentifier::new);
    }

    @Override
    public StackListResult<S> add(S template, long amount) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        I entry = idFactory.apply(template);

        S existing = entries.get(entry);
        if (existing != null) {
            return addToExisting(existing, amount);
        } else {
            return addNew(entry, template, amount);
        }
    }

    private StackListResult<S> addToExisting(S stack, long amount) {
        stack.increment(amount);

        return new StackListResult<>(stack, amount, index.inverse().get(stack), true);
    }

    private StackListResult<S> addNew(I entry, S template, long amount) {
        S stack = (S) template.copy();
        stack.setAmount(amount);

        UUID id = UUID.randomUUID();

        index.put(id, stack);
        entries.put(entry, stack);

        return new StackListResult<>(stack, amount, id, true);
    }

    @Override
    public Optional<StackListResult<S>> remove(S template, long amount) {
        if (template.isEmpty() || amount <= 0) {
            throw new IllegalArgumentException("Invalid stack");
        }

        I entry = idFactory.apply(template);

        S existing = entries.get(entry);
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

    private Optional<StackListResult<S>> removePartly(long amount, S stack, UUID id) {
        stack.decrement(amount);

        return Optional.of(new StackListResult<>(stack, -amount, id, true));
    }

    private Optional<StackListResult<S>> removeCompletely(I entry, S stack, UUID id) {
        index.remove(id);
        entries.remove(entry);

        return Optional.of(new StackListResult<>(stack, -stack.getAmount(), id, false));
    }

    @Override
    public Optional<S> get(S template) {
        return Optional.ofNullable(entries.get(idFactory.apply(template)));
    }

    @Override
    public Optional<S> get(UUID id) {
        return Optional.ofNullable(index.get(id));
    }

    @Override
    public Collection<S> getAll() {
        return entries.values();
    }

    @Override
    public void clear() {
        index.clear();
        entries.clear();
    }
}
