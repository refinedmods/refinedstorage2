package com.refinedmods.refinedstorage2.fabric.util;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.fabric.item.FabricRs2Item;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.registry.Registry;

public final class ItemStacks {
    private static final Map<Item, Rs2Item> ITEMS = new HashMap<>();

    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";

    private ItemStacks() {
    }

    public static Rs2ItemStack ofItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return Rs2ItemStack.EMPTY;
        }
        return new Rs2ItemStack(ofItem(stack.getItem()), stack.getCount(), stack.getNbt());
    }

    public static NbtCompound toTag(Rs2ItemStack stack) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_AMOUNT, stack.getAmount());
        if (stack.getTag() != null) {
            tag.put(TAG_TAG, (NbtCompound) stack.getTag());
        }
        tag.putInt(TAG_ID, stack.getItem().getId());
        return tag;
    }

    public static Rs2ItemStack fromTag(NbtCompound tag) {
        int id = tag.getInt(TAG_ID);
        Item item = Registry.ITEM.get(id);
        if (item == Items.AIR) {
            return Rs2ItemStack.EMPTY;
        }
        long amount = tag.getLong(TAG_AMOUNT);
        Object stackTag = tag.get(TAG_TAG);
        return new Rs2ItemStack(ofItem(item), amount, stackTag);
    }

    public static ItemStack toItemStack(Rs2ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack s = new ItemStack(toItem(stack.getItem()));
        s.setNbt((NbtCompound) stack.getTag());
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
