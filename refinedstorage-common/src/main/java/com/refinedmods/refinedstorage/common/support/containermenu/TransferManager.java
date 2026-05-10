package com.refinedmods.refinedstorage.common.support.containermenu;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TransferManager {
    private final Map<TransferDestination, List<TransferDestination>> destinationMap = new HashMap<>();
    private final AbstractContainerMenu containerMenu;
    private final Function<Container, TransferDestination> destinationFactory;

    public TransferManager(final AbstractContainerMenu containerMenu,
                           final Function<Container, TransferDestination> destinationFactory) {
        this.containerMenu = containerMenu;
        this.destinationFactory = destinationFactory;
    }

    public void addTransfer(final Container from, final Container to) {
        addTransfer(
            destinationFactory.apply(from),
            destinationFactory.apply(to)
        );
    }

    private void addTransfer(final TransferDestination from, final TransferDestination to) {
        final List<TransferDestination> destinationList = destinationMap.computeIfAbsent(from, k -> new LinkedList<>());
        destinationList.add(to);
    }

    public void addFilterTransfer(final Container from) {
        if (!(containerMenu instanceof AbstractResourceContainerMenu resourceContainer)) {
            throw new UnsupportedOperationException(containerMenu.getClass().toString());
        }
        addTransfer(destinationFactory.apply(from), new ResourceInventoryDestination(resourceContainer));
    }

    public void addBiTransfer(final Container from, final Container to) {
        addTransfer(from, to);
        addTransfer(to, from);
    }

    public boolean transfer(final int index) {
        final Slot slot = containerMenu.getSlot(index);
        if (slot.getItem().isEmpty()) {
            return false;
        }
        final TransferDestination key = destinationFactory.apply(slot.container);
        final List<TransferDestination> destinations = destinationMap.get(key);
        return destinations != null && transfer(slot, destinations);
    }

    private boolean transfer(final Slot slot, final List<TransferDestination> destinations) {
        final ItemStack initial = slot.getItem().copy();
        final ItemStack remainder = transfer(initial, destinations);
        slot.set(remainder);
        slot.setChanged();
        return initial.getCount() != remainder.getCount();
    }

    private ItemStack transfer(final ItemStack stack, final List<TransferDestination> destinations) {
        for (final TransferDestination destination : destinations) {
            final ItemStack remainder = destination.transfer(stack);
            final boolean mustStop = remainder == null;
            if (mustStop) {
                return stack;
            }
            final boolean didNotTransferAnything = ItemStack.matches(stack, remainder);
            if (didNotTransferAnything) {
                continue;
            }
            return remainder;
        }
        return stack;
    }

    public void clear() {
        destinationMap.clear();
    }
}
