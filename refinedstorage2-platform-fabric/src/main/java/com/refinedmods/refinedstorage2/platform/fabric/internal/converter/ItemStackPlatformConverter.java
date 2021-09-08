package com.refinedmods.refinedstorage2.platform.fabric.internal.converter;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.converter.PlatformConverter;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class ItemStackPlatformConverter implements PlatformConverter<ItemStack, Rs2ItemStack> {
    private final PlatformConverter<Item, Rs2Item> itemConverter;

    public ItemStackPlatformConverter(PlatformConverter<Item, Rs2Item> itemConverter) {
        this.itemConverter = itemConverter;
    }

    @Override
    public ItemStack toPlatform(Rs2ItemStack value) {
        if (value.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(Rs2PlatformApiFacade.INSTANCE.itemConversion().toPlatform(value.getItem()));
        stack.setNbt(value.getTag() != null ? (NbtCompound) value.getTag() : null);
        stack.setCount((int) value.getAmount());
        return stack;
    }

    @Override
    public Rs2ItemStack toDomain(ItemStack value) {
        if (value.isEmpty()) {
            return Rs2ItemStack.EMPTY;
        }
        return new Rs2ItemStack(itemConverter.toDomain(value.getItem()), value.getCount(), value.getNbt());
    }
}
