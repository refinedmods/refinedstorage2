package com.refinedmods.refinedstorage2.platform.common.internal.item;

import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class SlotReferenceImpl implements SlotReference {
    private final int slotIndex;

    public SlotReferenceImpl(final int slotIndex) {
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
    public boolean isDisabledSlot(final int playerSlotIndex) {
        return playerSlotIndex == slotIndex;
    }

    @Override
    public void writeToBuf(final ByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public static SlotReference of(final ByteBuf buf) {
        return new SlotReferenceImpl(buf.readInt());
    }

    public static SlotReference of(final Player player, final InteractionHand hand) {
        return new SlotReferenceImpl(hand == InteractionHand.MAIN_HAND
            ? player.getInventory().selected
            : Inventory.SLOT_OFFHAND);
    }
}
