package com.refinedmods.refinedstorage2.platform.common.item;


import com.refinedmods.refinedstorage2.platform.api.item.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import net.minecraft.world.item.Item;

public final class SimpleUpgradeItem extends AbstractUpgradeItem {
    public SimpleUpgradeItem(final UpgradeRegistry registry) {
        super(new Item.Properties(), registry);
    }
}
