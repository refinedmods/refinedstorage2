package com.refinedmods.refinedstorage2.platform.api.item;

import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface SlotReferenceProvider {
    List<SlotReference> find(Player player, Set<Item> validItems);
}
