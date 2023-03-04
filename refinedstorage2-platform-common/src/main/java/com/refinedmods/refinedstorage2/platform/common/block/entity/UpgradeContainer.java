package com.refinedmods.refinedstorage2.platform.common.block.entity;

import com.refinedmods.refinedstorage2.platform.api.upgrade.ApplicableUpgrade;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Set;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

public class UpgradeContainer extends SimpleContainer {
    private final UpgradeDestination destination;
    private final UpgradeRegistry registry;

    public UpgradeContainer(final UpgradeDestination destination,
                            final UpgradeRegistry registry) {
        this(destination, registry, () -> {
        });
    }

    public UpgradeContainer(final UpgradeDestination destination,
                            final UpgradeRegistry registry,
                            final Runnable listener) {
        super(4);
        this.destination = destination;
        this.registry = registry;
        this.addListener(container -> listener.run());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        final ApplicableUpgrade upgrade = getApplicableUpgrades()
            .stream()
            .filter(applicableUpgrade -> applicableUpgrade.itemSupplier().get() == stack.getItem())
            .findFirst()
            .orElse(null);
        if (upgrade == null) {
            return false;
        }
        final int currentCount = countItem(stack.getItem());
        if (currentCount >= upgrade.maxAmount()) {
            return false;
        }
        return super.canPlaceItem(slot, stack);
    }

    public Set<ApplicableUpgrade> getApplicableUpgrades() {
        return registry.getApplicableUpgrades(destination);
    }
}
