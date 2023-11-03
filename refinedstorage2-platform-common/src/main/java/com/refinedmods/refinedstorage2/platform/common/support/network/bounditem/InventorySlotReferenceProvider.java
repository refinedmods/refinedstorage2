package com.refinedmods.refinedstorage2.platform.common.support.network.bounditem;

import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class InventorySlotReferenceProvider implements SlotReferenceProvider {
    @Override
    public List<SlotReference> find(final Player player, final Set<Item> validItems) {
        final List<SlotReference> result = new ArrayList<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            final ItemStack slot = player.getInventory().getItem(i);
            if (!validItems.contains(slot.getItem())) {
                continue;
            }
            result.add(new InventorySlotReference(i));
        }
        return result;
    }
}
