package com.refinedmods.refinedstorage2.fabric.util;

import java.util.HashMap;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.item.Rs2Item;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.fabric.item.FabricRs2Item;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

public final class ItemStacks {
    private static final Map<Item, Rs2Item> ITEMS = new HashMap<>();

    private ItemStacks() {
    }

    public static Rs2ItemStack ofItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return Rs2ItemStack.EMPTY;
        }
        return new Rs2ItemStack(ofItem(stack.getItem()), stack.getCount(), stack.getTag());
    }

    public static ItemStack toItemStack(Rs2ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack s = new ItemStack(toItem(stack.getItem()));
        s.setTag((CompoundTag) stack.getTag());
        s.setCount((int) stack.getAmount());
        return s;
    }

    public static Rs2Item ofItem(Item item) {
        return ITEMS.computeIfAbsent(item, FabricRs2Item::new);
    }

    public static Item toItem(Rs2Item item) {
        return ((FabricRs2Item) item).getItem();
    }
}
