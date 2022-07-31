package com.refinedmods.refinedstorage2.platform.common.containermenu.transfer;

import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractResourceFilterContainerMenu;

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
        if (!(containerMenu instanceof AbstractResourceFilterContainerMenu resourceFilterContainer)) {
            throw new UnsupportedOperationException(
                "Cannot add filter transfer if container menu isn't resource filterable"
            );
        }
        addTransfer(
            destinationFactory.apply(from),
            new FilterInventoryDestination(resourceFilterContainer)
        );
    }

    public void addBiTransfer(final Container from, final Container to) {
        addTransfer(from, to);
        addTransfer(to, from);
    }

    public void transfer(final int index) {
        final Slot slot = containerMenu.getSlot(index);
        if (slot.getItem().isEmpty()) {
            return;
        }
        final TransferDestination key = destinationFactory.apply(slot.container);
        final List<TransferDestination> destinations = destinationMap.get(key);
        if (destinations != null) {
            transfer(slot, destinations);
        }
    }

    private void transfer(final Slot slot, final List<TransferDestination> destinations) {
        final ItemStack initial = slot.getItem().copy();
        final ItemStack remainder = doTransfer(initial, destinations);
        slot.set(remainder);
        slot.setChanged();
    }

    private ItemStack doTransfer(final ItemStack initial, final List<TransferDestination> destinations) {
        ItemStack remainder = initial.copy();
        for (final TransferDestination destination : destinations) {
            final ItemStack destinationRemainder = destination.transfer(remainder);
            if (destinationRemainder == null) {
                break;
            }
            remainder = destinationRemainder;
            if (remainder.isEmpty()) {
                break;
            }
        }
        return remainder;
    }
}
