package com.refinedmods.refinedstorage2.platform.common.support.networkbounditem;

import com.refinedmods.refinedstorage2.platform.api.support.networkbounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.networkbounditem.SlotReferenceFactory;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InventorySlotReference implements SlotReference {
    private final int slotIndex;

    public InventorySlotReference(final int slotIndex) {
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

    @Override
    public void writeToBuffer(final FriendlyByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public static SlotReference of(final Player player, final InteractionHand hand) {
        return new InventorySlotReference(hand == InteractionHand.MAIN_HAND
            ? player.getInventory().selected
            : Inventory.SLOT_OFFHAND);
    }
}
