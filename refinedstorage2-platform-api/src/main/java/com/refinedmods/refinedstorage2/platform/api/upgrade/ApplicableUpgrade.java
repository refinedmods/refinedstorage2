package com.refinedmods.refinedstorage2.platform.api.upgrade;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;

public record ApplicableUpgrade(Supplier<Item> itemSupplier, int maxAmount) {
}
