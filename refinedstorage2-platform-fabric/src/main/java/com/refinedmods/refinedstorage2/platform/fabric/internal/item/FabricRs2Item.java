package com.refinedmods.refinedstorage2.platform.fabric.internal.item;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;

import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class FabricRs2Item implements Rs2Item {
    private final Item item;
    private final Identifier identifier;
    private final int rawId;

    public FabricRs2Item(Item item) {
        this.item = item;
        this.rawId = Item.getRawId(item);
        this.identifier = Registry.ITEM.getId(item);
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
        return rawId;
    }

    @Override
    public String getIdentifier() {
        return identifier.toString();
    }

    @Override
    public String toString() {
        return item.toString();
    }
}
