package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.item.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import net.minecraft.world.item.Item;

// TODO: improved creative texture.
public class RangeUpgradeItem extends AbstractUpgradeItem {
    private final boolean creative;

    public RangeUpgradeItem(final UpgradeRegistry registry, final boolean creative) {
        super(new Item.Properties(), registry);
        this.creative = creative;
    }

    @Override
    public long getEnergyUsage() {
        if (creative) {
            return Platform.INSTANCE.getConfig().getUpgrade().getCreativeRangeUpgradeEnergyUsage();
        }
        return Platform.INSTANCE.getConfig().getUpgrade().getRangeUpgradeEnergyUsage();
    }
}
