package com.refinedmods.refinedstorage2.core.storage;

import com.refinedmods.refinedstorage2.core.list.StackList;
import com.refinedmods.refinedstorage2.core.list.item.ItemStackList;
import com.refinedmods.refinedstorage2.core.util.Action;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ItemStorageChannel implements StorageChannel<ItemStack> {
    private final StackList<ItemStack> list = new ItemStackList();
    private List<Storage<ItemStack>> sources = Collections.emptyList();

    @Override
    public Optional<ItemStack> extract(ItemStack template, int amount, Action action) {
        return Optional.empty();
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
    public StackList<ItemStack> getList() {
        return list;
    }

    @Override
    public void setSources(List<Storage<ItemStack>> sources) {
        this.sources = sources;
    }
}
