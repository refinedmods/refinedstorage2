package com.refinedmods.refinedstorage.common.support.slotreference;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;
import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReferenceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventoryPlayerSlotReferenceProvider implements PlayerSlotReferenceProvider {
    @Override
    public List<PlayerSlotReference> find(final Player player, final Set<Item> validItems) {
        final List<PlayerSlotReference> result = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!validItems.contains(slot.getItem())) {
                continue;
            }
            result.add(new InventoryPlayerSlotReference(i));
        }
        return result;
    }
}
