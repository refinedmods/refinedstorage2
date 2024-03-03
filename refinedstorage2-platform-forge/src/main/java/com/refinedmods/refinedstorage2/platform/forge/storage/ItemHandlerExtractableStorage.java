package com.refinedmods.refinedstorage2.platform.forge.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.ExtractableStorage;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemHandlerExtractableStorage implements ExtractableStorage {
    private final CapabilityCache capabilityCache;
    private final AmountOverride amountOverride;

    public ItemHandlerExtractableStorage(final CapabilityCache capabilityCache,
                                         final AmountOverride amountOverride) {
        this.capabilityCache = capabilityCache;
        this.amountOverride = amountOverride;
    }

    @Override
    public long extract(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!(resource instanceof ItemResource itemResource)) {
            return 0L;
        }
        return capabilityCache.getItemHandler().map(itemHandler -> {
            final ItemStack toExtractStack = itemResource.toItemStack(amount);
            final long correctedAmount = amountOverride.overrideAmount(
                resource,
                amount,
                () -> ForgeHandlerUtil.getCurrentAmount(itemHandler, toExtractStack)
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
