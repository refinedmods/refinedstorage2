package com.refinedmods.refinedstorage2.fabric.coreimpl.grid;

import alexiil.mc.lib.attributes.Simulation;
import alexiil.mc.lib.attributes.item.compat.FixedInventoryVanillaWrapper;
import alexiil.mc.lib.attributes.item.filter.ExactItemStackFilter;
import com.refinedmods.refinedstorage2.core.grid.GridInteractor;
import com.refinedmods.refinedstorage2.core.util.Action;
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
    public ItemStack insertIntoInventory(ItemStack stack, int preferredSlot, Action action) {
        Simulation simulation = getSimulation(action);

        if (preferredSlot == -1) {
            return inventory.getInsertable().attemptInsertion(stack, simulation);
        }

        // TODO: Prevent this going into the armor slots.
        ItemStack remainder = inventory.insertStack(preferredSlot, stack, simulation);
        if (!remainder.isEmpty()) {
            return inventory.getInsertable().attemptInsertion(remainder, simulation);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack extractFromInventory(ItemStack template, int slot, int count, Action action) {
        Simulation simulation = getSimulation(action);

        return slot == -1 ?
            inventory.getExtractable().attemptExtraction(new ExactItemStackFilter(template), count, simulation) :
            inventory.extractStack(slot, null, ItemStack.EMPTY, count, simulation);
    }

    private Simulation getSimulation(Action action) {
        return action == Action.EXECUTE ? Simulation.ACTION : Simulation.SIMULATE;
    }

    @Override
    public String getName() {
        return player.getGameProfile().getName();
    }
}
