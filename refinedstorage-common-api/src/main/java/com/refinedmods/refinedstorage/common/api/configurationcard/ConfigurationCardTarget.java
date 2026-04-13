package com.refinedmods.refinedstorage.common.api.configurationcard;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.apiguardian.api.API;

/**
 * Implement this on a block entity that supports the configuration card.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.2")
public interface ConfigurationCardTarget {
    void writeConfiguration(ValueOutput output);

    void readConfiguration(ValueInput input);

    default List<ItemStack> getUpgrades() {
        return Collections.emptyList();
    }

    default boolean addUpgrade(final ItemStack upgradeStack) {
        return false;
    }
}
