package com.refinedmods.refinedstorage2.api.stack.test;

import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;

public final class ItemStubs {
    public static final Rs2Item DIRT = new ItemStub(1, "Dirt", 64, "dirt");
    public static final Rs2Item FURNACE = new ItemStub(2, "Furnace", 64, "furnace");
    public static final Rs2Item GLASS = new ItemStub(3, "Glass", 64, "glass");
    public static final Rs2Item COBBLESTONE = new ItemStub(4, "Cobblestone", 64, "cobblestone");
    public static final Rs2Item SPONGE = new ItemStub(5, "Sponge", 64, "sponge");
    public static final Rs2Item BUCKET = new ItemStub(6, "Bucket", 16, "bucket");
    public static final Rs2Item DIAMOND = new ItemStub(7, "Diamond", 64, "diamond");
    public static final Rs2Item STONE = new ItemStub(8, "Stone", 64, "stone");
    public static final Rs2Item SADDLE = new ItemStub(9, "Saddle", 1, "saddle");
    public static final Rs2Item GOLD_BLOCK = new ItemStub(10, "Gold Block", 64, "gold_block");

    private ItemStubs() {
    }
}