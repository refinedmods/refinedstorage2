package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;

import net.minecraft.world.entity.player.Player;

interface CraftingGridRefillContext extends AutoCloseable {
    boolean extract(ItemResource resource, Player player);

    @Override
    void close();
}
