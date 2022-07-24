package com.refinedmods.refinedstorage2.platform.common.item;


import com.refinedmods.refinedstorage2.platform.api.item.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public final class SimpleUpgradeItem extends AbstractUpgradeItem {
    public SimpleUpgradeItem(final CreativeModeTab tab, final UpgradeRegistry registry) {
        super(new Item.Properties().tab(tab), registry);
    }
}
