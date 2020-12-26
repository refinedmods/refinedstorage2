package com.refinedmods.refinedstorage2.fabric.coreimpl.grid;

import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import com.refinedmods.refinedstorage2.core.grid.GridInteractor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerGridInteractor implements GridInteractor {
    private final PlayerEntity player;
    private final FixedInventoryVanillaWrapper inventory;

    public PlayerGridInteractor(PlayerEntity player) {
        this.player = player;
        this.inventory = new FixedInventoryVanillaWrapper(player.inventory);
    }

    @Override
    public ItemStack getCursorStack() {
        return player.inventory.getCursorStack();
    }

    @Override
    public void setCursorStack(ItemStack stack) {
        player.inventory.setCursorStack(stack);
        ((ServerPlayerEntity) player).updateCursorStack();
    }

    @Override
    public ItemStack insertIntoInventory(ItemStack stack) {
        return inventory.getInsertable().insert(stack);
    }

    @Override
    public ItemStack extractFromInventory(ItemStack template, int count) {
        return inventory.getExtractable().extract(template, count);
    }
}
