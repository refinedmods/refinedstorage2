package com.refinedmods.refinedstorage2.platform.api.upgrade;

import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public interface UpgradeRegistry {
    void addApplicableUpgrade(UpgradeDestination destination, Supplier<Item> itemSupplier, int maxAmount);

    Set<ApplicableUpgrade> getApplicableUpgrades(UpgradeDestination destination);

    Set<UpgradeInDestination> getDestinations(Item item);
}
