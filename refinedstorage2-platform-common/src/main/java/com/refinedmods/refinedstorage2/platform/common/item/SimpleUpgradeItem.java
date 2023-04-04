package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.item.AbstractUpgradeItem;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.function.LongSupplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SimpleUpgradeItem extends AbstractUpgradeItem {
    private final LongSupplier energyUsageResolver;
    private final boolean foil;

    public SimpleUpgradeItem(final UpgradeRegistry registry,
                             final LongSupplier energyUsageResolver,
                             final boolean foil) {
        super(new Item.Properties(), registry);
        this.energyUsageResolver = energyUsageResolver;
        this.foil = foil;
    }

    @Override
    public long getEnergyUsage() {
        return energyUsageResolver.getAsLong();
    }

    @Override
    public boolean isFoil(final ItemStack stack) {
        return foil;
    }
}
