package com.refinedmods.refinedstorage2.platform.api.upgrade;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public interface UpgradeDestination {
    Component getName();

    ItemStack getStackRepresentation();
}
