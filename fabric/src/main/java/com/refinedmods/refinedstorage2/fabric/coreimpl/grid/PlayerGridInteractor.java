package com.refinedmods.refinedstorage2.fabric.coreimpl.grid;

import com.refinedmods.refinedstorage2.core.grid.GridInteractor;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.util.Action;
import com.refinedmods.refinedstorage2.fabric.util.ItemStacks;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;
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
    public Rs2ItemStack getCursorStack() {
        return ItemStacks.ofItemStack(player.inventory.getCursorStack());
    }

    @Override
    public void setCursorStack(Rs2ItemStack stack) {
        player.inventory.setCursorStack(ItemStacks.toItemStack(stack));
        ((ServerPlayerEntity) player).updateCursorStack();
    }

    @Override
    public Rs2ItemStack insertIntoInventory(Rs2ItemStack stack, int preferredSlot, Action action) {
        Simulation simulation = getSimulation(action);
        ItemStack mcStack = ItemStacks.toItemStack(stack);

        if (preferredSlot == -1) {
            return ItemStacks.ofItemStack(inventory.getInsertable().attemptInsertion(mcStack, simulation));
        }

        // TODO: Prevent this going into the armor slots.
        ItemStack remainder = inventory.insertStack(preferredSlot, mcStack, simulation);
        if (!remainder.isEmpty()) {
            return ItemStacks.ofItemStack(inventory.getInsertable().attemptInsertion(remainder, simulation));
        }
        return Rs2ItemStack.EMPTY;
    }

    @Override
    public Rs2ItemStack extractFromInventory(Rs2ItemStack template, int slot, long count, Action action) {
        Simulation simulation = getSimulation(action);
        ItemStack mcTemplate = ItemStacks.toItemStack(template);

        return slot == -1 ?
                ItemStacks.ofItemStack(inventory.getExtractable().attemptExtraction(new ExactItemStackFilter(mcTemplate), (int) count, simulation)) :
                ItemStacks.ofItemStack(inventory.extractStack(slot, null, ItemStack.EMPTY, (int) count, simulation));
    }

    private Simulation getSimulation(Action action) {
        return action == Action.EXECUTE ? Simulation.ACTION : Simulation.SIMULATE;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
