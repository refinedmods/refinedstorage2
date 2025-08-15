package com.refinedmods.refinedstorage.neoforge.storage;

import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemHandlerExtractableStorage implements ExtractableStorage {
    private final CapabilityCache capabilityCache;

    public ItemHandlerExtractableStorage(final CapabilityCache capabilityCache) {
        this.capabilityCache = capabilityCache;
    }

    public long getAmount(final ResourceKey resource) {
        if (!(resource instanceof ItemResource itemResource)) {
            return 0;
        }
        return capabilityCache.getItemHandler()
            .map(itemHandler -> ForgeHandlerUtil.getCurrentAmount(itemHandler, itemResource.toItemStack()))
            .orElse(0L);
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!(resource instanceof ItemResource itemResource)) {
            return 0L;
        }
        return capabilityCache.getItemHandler()
            .map(itemHandler -> extract(amount, action, itemHandler, itemResource.toItemStack(amount)))
            .orElse(0L);
    }

    private long extract(final long amount,
                         final Action action,
                         final IItemHandler itemHandler,
                         final ItemStack stack) {
        long extracted = 0;
        for (int slot = 0; slot < itemHandler.getSlots(); ++slot) {
            final ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            final boolean relevant = ItemStack.isSameItemSameComponents(stackInSlot, stack);
            if (!relevant) {
                continue;
            }
            final long toExtract = amount - extracted;
            extracted += extractSlot(action, itemHandler, stackInSlot, slot, (int) toExtract);
            if (extracted >= amount) {
                break;
            }
        }
        return extracted;
    }

    private int extractSlot(final Action action, final IItemHandler itemHandler, final ItemStack stackInSlot,
                            final int slot, final int amount) {
        return switch (action) {
            case SIMULATE -> {
                final int amountInSlot = stackInSlot.getCount();
                final int maxStackSize = stackInSlot.getMaxStackSize();
                final int extracted = itemHandler.extractItem(slot, amount, true).getCount();
                // If we want to extract more than the maximum stack size, and we were able to extract
                // the maximum stack size, and the slot has more than the maximum stack size,
                // we will assume that we can extract more.
                if (amount > maxStackSize && extracted == maxStackSize && amountInSlot > maxStackSize) {
                    yield Math.min(amount, amountInSlot);
                }
                yield extracted;
            }
            case EXECUTE -> {
                int totalExtracted = 0;
                while (totalExtracted < amount) {
                    final int toExtract = amount - totalExtracted;
                    final int extracted = itemHandler.extractItem(slot, toExtract, false).getCount();
                    if (extracted <= 0) {
                        break;
                    }
                    totalExtracted += extracted;
                }
                yield totalExtracted;
            }
        };
    }
}
