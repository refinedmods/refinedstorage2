package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.forge.util.VariantUtil.toItemStack;

public class ItemHandlerExtractableStorage implements ExtractableStorage<ItemResource> {
    private final InteractionCoordinates interactionCoordinates;

    public ItemHandlerExtractableStorage(final InteractionCoordinates interactionCoordinates) {
        this.interactionCoordinates = interactionCoordinates;
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates.getItemHandler().map(itemHandler -> {
            final ItemStack toExtractStack = toItemStack(resource, amount);
            long extracted = 0;
            for (int slot = 0; slot < itemHandler.getSlots(); ++slot) {
                final boolean relevant = isSame(itemHandler.getStackInSlot(slot), toExtractStack);
                if (!relevant) {
                    continue;
                }
                final long toExtract = amount - extracted;
                extracted += itemHandler.extractItem(slot, (int) toExtract, action == Action.SIMULATE).getCount();
                if (extracted >= amount) {
                    break;
                }
            }
            return extracted;
        }).orElse(0L);
    }

    private static boolean isSame(final ItemStack a, final ItemStack b) {
        return ItemStack.isSameItemSameTags(a, b);
    }
}
