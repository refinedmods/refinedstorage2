package com.refinedmods.refinedstorage.common.api.support.slotreference;

import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
@FunctionalInterface
public interface PlayerSlotReferenceProvider {
    List<PlayerSlotReference> find(Player player, Set<Item> validItems);
}
