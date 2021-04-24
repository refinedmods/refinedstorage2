package com.refinedmods.refinedstorage2.fabric.item;

import com.refinedmods.refinedstorage2.core.item.Rs2Item;

import net.minecraft.item.Item;

public class FabricRs2Item implements Rs2Item {
    private final Item item;

    public FabricRs2Item(Item item) {
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    @Override
    public int getMaxAmount() {
        return item.getMaxCount();
    }

    @Override
    public int getId() {
        return Item.getRawId(item);
    }

    @Override
    public String getName() {
        return item.getName().getString();
    }

    @Override
    public String toString() {
        return item.toString();
    }
}
