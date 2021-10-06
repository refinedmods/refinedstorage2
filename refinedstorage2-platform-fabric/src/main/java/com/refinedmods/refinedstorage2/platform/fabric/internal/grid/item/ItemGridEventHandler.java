package com.refinedmods.refinedstorage2.platform.fabric.internal.grid.item;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.resource.ItemResource;

import net.minecraft.item.ItemStack;

public interface ItemGridEventHandler {
    void insert(GridInsertMode insertMode);

    ItemStack transfer(ItemStack stack);

    void onExtract(ItemResource itemResource, GridExtractMode mode, boolean cursor);
}
