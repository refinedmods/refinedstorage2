package com.refinedmods.refinedstorage.common.api.upgrade;

import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public record UpgradeMapping(UpgradeDestination destination, Item upgradeItem, int maxAmount) {
}
