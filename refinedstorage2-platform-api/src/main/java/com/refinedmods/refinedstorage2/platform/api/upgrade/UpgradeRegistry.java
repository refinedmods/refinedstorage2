package com.refinedmods.refinedstorage2.platform.api.upgrade;

import java.util.Set;

import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public interface UpgradeRegistry {
    DestinationBuilder forDestination(UpgradeDestination destination);

    Set<UpgradeMapping> getByDestination(UpgradeDestination destination);

    Set<UpgradeMapping> getByUpgradeItem(Item upgradeItem);

    @FunctionalInterface
    interface DestinationBuilder {
        DestinationBuilder add(Item upgradeItem, int maxAmount);

        default DestinationBuilder add(Item upgradeItem) {
            return add(upgradeItem, 1);
        }
    }
}
