package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PlayerSlotReference {
    private final int slotIndex;

    private PlayerSlotReference(final int slotIndex) {
        this.slotIndex = slotIndex;
    }

    public ItemStack resolve(final Player player) {
        return player.getInventory().getItem(slotIndex);
    }

    public boolean isDisabled(final int playerSlotIndex) {
        return playerSlotIndex == slotIndex;
    }

    public void writeToBuf(final ByteBuf buf) {
        buf.writeInt(slotIndex);
    }

    public static PlayerSlotReference of(final ByteBuf buf) {
        return new PlayerSlotReference(buf.readInt());
    }

    public static PlayerSlotReference of(final Player player, final InteractionHand hand) {
        return new PlayerSlotReference(hand == InteractionHand.MAIN_HAND
            ? player.getInventory().selected
            : Inventory.SLOT_OFFHAND);
    }
}
