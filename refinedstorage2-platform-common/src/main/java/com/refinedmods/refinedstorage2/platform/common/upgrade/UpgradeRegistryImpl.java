package com.refinedmods.refinedstorage2.platform.common.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class UpgradeRegistryImpl implements UpgradeRegistry {
    private final Map<UpgradeDestination, Set<UpgradeMapping>> byDestination = new HashMap<>();
    private final Map<Item, Set<UpgradeMapping>> byUpgradeItem = new HashMap<>();

    @Override
    public DestinationBuilder forDestination(final UpgradeDestination destination) {
        return new DestinationBuilder() {
            @Override
            public DestinationBuilder add(final Item upgradeItem, final int maxAmount) {
                final UpgradeMapping mapping = createMapping(destination, upgradeItem, maxAmount);
                byDestination.computeIfAbsent(destination, key -> new HashSet<>()).add(mapping);
                byUpgradeItem.computeIfAbsent(upgradeItem, key -> new HashSet<>()).add(mapping);
                return this;
            }
        };
    }

    private static UpgradeMapping createMapping(final UpgradeDestination destination,
                                                final Item upgradeItem,
                                                final int maxAmount) {
        final ItemStack displayItemStack = new ItemStack(upgradeItem);
        return new UpgradeMapping(
            destination,
            upgradeItem,
            maxAmount,
            upgradeItem.getName(displayItemStack).copy()
                .append(" ")
                .append("(")
                .append(String.valueOf(maxAmount))
                .append(")"),
            destination.getName().copy()
                .append(" ")
                .append("(")
                .append(String.valueOf(maxAmount))
                .append(")"),
            displayItemStack
        );
    }

    @Override
    public Set<UpgradeMapping> getByDestination(final UpgradeDestination destination) {
        return byDestination.getOrDefault(destination, Collections.emptySet());
    }

    @Override
    public Set<UpgradeMapping> getByUpgradeItem(final Item upgradeItem) {
        return byUpgradeItem.getOrDefault(upgradeItem, Collections.emptySet());
    }
}
