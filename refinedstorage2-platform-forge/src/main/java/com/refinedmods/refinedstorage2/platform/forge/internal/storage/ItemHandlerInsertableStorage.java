package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemHandlerInsertableStorage implements InsertableStorage<ItemResource> {
    private final InteractionCoordinates interactionCoordinates;

    public ItemHandlerInsertableStorage(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates.getItemHandler().map(itemHandler -> {
            final ItemStack stack = resource.toItemStack(amount);
            final ItemStack remainder = ItemHandlerHelper.insertItem(
                itemHandler,
                stack,
                action == Action.SIMULATE
            );
            return amount - remainder.getCount();
        }).orElse(0L);
    }
}
