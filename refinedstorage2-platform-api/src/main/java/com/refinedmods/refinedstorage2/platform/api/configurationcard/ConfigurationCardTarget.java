package com.refinedmods.refinedstorage2.platform.api.configurationcard;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

/**
 * Implement this on a block entity that supports the configuration card.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.2")
public interface ConfigurationCardTarget {
    void writeConfiguration(CompoundTag tag);

    void readConfiguration(CompoundTag tag);

    List<Item> getUpgradeItems();

    boolean addUpgradeItem(Item upgradeItem);
}
