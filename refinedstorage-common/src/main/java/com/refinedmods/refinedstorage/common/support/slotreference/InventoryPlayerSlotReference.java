package com.refinedmods.refinedstorage.common.support.slotreference;

import com.refinedmods.refinedstorage.common.api.support.slotreference.PlayerSlotReference;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class InventoryPlayerSlotReference implements PlayerSlotReference {
    public static final StreamCodec<RegistryFriendlyByteBuf, InventoryPlayerSlotReference> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.INT, slotReference -> slotReference.slotIndex,
            InventoryPlayerSlotReference::new
        );

    final int slotIndex;

    InventoryPlayerSlotReference(final int slotIndex) {
        this.slotIndex = slotIndex;
    }

    @Override
    public ItemStack get(final Player player) {
        return player.getInventory().getItem(slotIndex);
    }

    @Override
    public void set(final Player player, final ItemStack stack) {
        player.getInventory().setItem(slotIndex, stack);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ? extends PlayerSlotReference> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public boolean isDisabled(final int playerSlotIndex) {
        return playerSlotIndex == slotIndex;
    }

    public static PlayerSlotReference of(final Player player, final InteractionHand hand) {
        return new InventoryPlayerSlotReference(hand == InteractionHand.MAIN_HAND
            ? player.getInventory().getSelectedSlot()
            : Inventory.SLOT_OFFHAND);
    }
}
