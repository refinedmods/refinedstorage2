package com.refinedmods.refinedstorage2.platform.api.upgrade;

import java.util.Set;

import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public interface UpgradeRegistry {
    void add(UpgradeDestination destination, Item upgradeItem, int maxAmount);

    Set<UpgradeMapping> getByDestination(UpgradeDestination destination);

    Set<UpgradeMapping> getByUpgradeItem(Item upgradeItem);
}
