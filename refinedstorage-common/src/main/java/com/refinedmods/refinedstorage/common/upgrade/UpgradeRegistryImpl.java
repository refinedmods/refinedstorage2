package com.refinedmods.refinedstorage.common.upgrade;

import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeMapping;
import com.refinedmods.refinedstorage.common.api.upgrade.UpgradeRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.world.item.Item;

public class UpgradeRegistryImpl implements UpgradeRegistry {
    private final Map<UpgradeDestination, Set<UpgradeMapping>> byDestination = new ConcurrentHashMap<>();
    private final Map<Item, Set<UpgradeMapping>> byUpgradeItem = new ConcurrentHashMap<>();

    @Override
    public DestinationBuilder forDestination(final UpgradeDestination destination) {
        return new DestinationBuilder() {
            @Override
            public DestinationBuilder add(final Item upgradeItem, final int maxAmount) {
                final UpgradeMapping mapping = createMapping(destination, upgradeItem, maxAmount);
                byDestination.computeIfAbsent(destination, key ->
                    Collections.synchronizedSet(new LinkedHashSet<>())).add(mapping);
                byUpgradeItem.computeIfAbsent(upgradeItem, key ->
                    Collections.synchronizedSet(new LinkedHashSet<>())).add(mapping);
                return this;
            }
        };
    }

    private static UpgradeMapping createMapping(final UpgradeDestination destination, final Item upgradeItem,
                                                final int maxAmount) {
        return new UpgradeMapping(destination, upgradeItem, maxAmount);
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
