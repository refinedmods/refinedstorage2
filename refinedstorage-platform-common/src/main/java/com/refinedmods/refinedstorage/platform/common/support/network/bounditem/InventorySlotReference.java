package com.refinedmods.refinedstorage.platform.common.support.network.bounditem;

import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceFactory;

import java.util.Optional;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InventorySlotReference implements SlotReference {
    final int slotIndex;

    InventorySlotReference(final int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    public Optional<ItemStack> resolve(final Player player) {
        final ItemStack item = player.getInventory().getItem(slotIndex);
        if (item.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(item);
    }

    @Override
    public SlotReferenceFactory getFactory() {
        return InventorySlotReferenceFactory.INSTANCE;
    }

    @Override
    public boolean isDisabledSlot(final int playerSlotIndex) {
        return playerSlotIndex == slotIndex;
    }

    public static SlotReference of(final Player player, final InteractionHand hand) {
        return new InventorySlotReference(hand == InteractionHand.MAIN_HAND
            ? player.getInventory().selected
            : Inventory.SLOT_OFFHAND);
    }
}
