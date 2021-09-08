package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

// TODO add test
public final class ItemStacks {
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";

    private ItemStacks() {
    }

    public static NbtCompound toTag(Rs2ItemStack stack) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_AMOUNT, stack.getAmount());
        if (stack.getTag() != null) {
            tag.put(TAG_TAG, (NbtCompound) stack.getTag());
        }
        tag.putString(TAG_ID, stack.getItem().getIdentifier());
        return tag;
    }

    public static Rs2ItemStack fromTag(NbtCompound tag) {
        Identifier id = new Identifier(tag.getString(TAG_ID));
        Item item = Registry.ITEM.get(id);
        if (item == Items.AIR) {
            return Rs2ItemStack.EMPTY;
        }
        long amount = tag.getLong(TAG_AMOUNT);
        Object stackTag = tag.get(TAG_TAG);
        return new Rs2ItemStack(Rs2PlatformApiFacade.INSTANCE.itemConversion().toDomain(item), amount, stackTag);
    }
}
