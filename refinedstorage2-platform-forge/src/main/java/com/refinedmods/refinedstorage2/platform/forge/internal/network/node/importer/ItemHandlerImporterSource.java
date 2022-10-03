package com.refinedmods.refinedstorage2.platform.forge.internal.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.network.node.importer.ImporterSource;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.InteractionCoordinates;
import com.refinedmods.refinedstorage2.platform.forge.internal.storage.ItemHandlerInsertableStorage;

import java.util.Collections;
import java.util.Iterator;
import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.ofItemStack;
import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toItemStack;

public class ItemHandlerImporterSource implements ImporterSource<ItemResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final ItemHandlerInsertableStorage insertTarget;

    public ItemHandlerImporterSource(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
        this.insertTarget = new ItemHandlerInsertableStorage(interactionCoordinates);
    }

    @Override
    public Iterator<ItemResource> getResources() {
        final LazyOptional<IItemHandler> possibleItemHandler = interactionCoordinates.getItemHandler();
        return possibleItemHandler.map(itemHandler -> (Iterator<ItemResource>) new AbstractIterator<ItemResource>() {
            private int index;

            @Nullable
            @Override
            protected ItemResource computeNext() {
                if (index > itemHandler.getSlots()) {
                    return endOfData();
                }
                for (; index < itemHandler.getSlots(); ++index) {
                    final ItemStack slot = itemHandler.getStackInSlot(index);
                    if (!slot.isEmpty()) {
                        index++;
                        return ofItemStack(slot);
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates.getItemHandler().map(itemHandler -> {
            final ItemStack stack = toItemStack(resource, amount);
            long extracted = 0;
            for (int i = 0; i < itemHandler.getSlots() && extracted < amount; ++i) {
                final ItemStack slot = itemHandler.getStackInSlot(i);
                if (ItemStack.isSameItemSameTags(slot, stack)) {
                    extracted += itemHandler.extractItem(
                        i,
                        (int) amount - (int) extracted,
                        action == Action.SIMULATE
                    ).getCount();
                }
            }
            return extracted;
        }).orElse(0L);
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return insertTarget.insert(resource, amount, action, actor);
    }
}
