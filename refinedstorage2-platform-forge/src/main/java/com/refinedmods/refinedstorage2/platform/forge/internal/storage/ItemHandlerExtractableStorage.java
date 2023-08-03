package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ItemHandlerExtractableStorage implements ExtractableStorage<ItemResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final AmountOverride amountOverride;

    public ItemHandlerExtractableStorage(final InteractionCoordinates interactionCoordinates,
                                         final AmountOverride amountOverride) {
        this.interactionCoordinates = interactionCoordinates;
        this.amountOverride = amountOverride;
    }

    @Override
    public long extract(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates.getItemHandler().map(itemHandler -> {
            final ItemStack toExtractStack = resource.toItemStack(amount);
            final long correctedAmount = amountOverride.overrideAmount(
                resource,
                amount,
                () -> HandlerUtil.getCurrentAmount(itemHandler, toExtractStack)
            );
            if (correctedAmount == 0) {
                return 0L;
            }
            return doExtract(correctedAmount, action, itemHandler, toExtractStack);
        }).orElse(0L);
    }

    private long doExtract(final long amount,
                           final Action action,
                           final IItemHandler itemHandler,
                           final ItemStack toExtractStack) {
        long extracted = 0;
        for (int slot = 0; slot < itemHandler.getSlots(); ++slot) {
            final boolean relevant = ItemStack.isSameItemSameTags(itemHandler.getStackInSlot(slot), toExtractStack);
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
    }
}
