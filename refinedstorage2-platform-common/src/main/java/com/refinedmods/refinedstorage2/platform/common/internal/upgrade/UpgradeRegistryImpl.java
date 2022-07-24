package com.refinedmods.refinedstorage2.platform.common.internal.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.ApplicableUpgrade;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeInDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;

public class UpgradeRegistryImpl implements UpgradeRegistry {
    private final Map<UpgradeDestination, Set<ApplicableUpgrade>> destinationMap = new HashMap<>();

    @Override
    public void addApplicableUpgrade(final UpgradeDestination destination,
                                     final Supplier<Item> itemSupplier,
                                     final int maxAmount) {
        destinationMap.computeIfAbsent(destination, key -> new HashSet<>())
            .add(new ApplicableUpgrade(itemSupplier, maxAmount));
    }

    @Override
    public Set<ApplicableUpgrade> getApplicableUpgrades(final UpgradeDestination destination) {
        return destinationMap.getOrDefault(destination, Collections.emptySet());
    }

    @Override
    public Set<UpgradeInDestination> getDestinations(final Item item) {
        final Set<UpgradeInDestination> result = new HashSet<>();
        for (final var entry : destinationMap.entrySet()) {
            final UpgradeDestination destination = entry.getKey();
            for (final ApplicableUpgrade applicableUpgrade : entry.getValue()) {
                if (applicableUpgrade.itemSupplier().get() == item) {
                    result.add(new UpgradeInDestination(destination, applicableUpgrade.maxAmount()));
                }
            }
        }
        return result;
    }
}
