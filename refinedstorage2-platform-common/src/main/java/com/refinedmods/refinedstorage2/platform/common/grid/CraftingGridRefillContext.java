package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

import net.minecraft.world.entity.player.Player;

public interface CraftingGridRefillContext extends AutoCloseable {
    boolean extract(ItemResource resource, Player player);

    @Override
    void close();
}
