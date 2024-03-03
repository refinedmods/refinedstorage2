package com.refinedmods.refinedstorage2.platform.forge.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class ItemHandlerInsertableStorage implements InsertableStorage {
    private final CapabilityCache capabilityCache;
    private final AmountOverride amountOverride;

    public ItemHandlerInsertableStorage(final CapabilityCache capabilityCache,
                                        final AmountOverride amountOverride) {
        this.capabilityCache = capabilityCache;
        this.amountOverride = amountOverride;
    }

    @Override
    public long insert(final ResourceKey resource, final long amount, final Action action, final Actor actor) {
        if (!(resource instanceof ItemResource itemResource)) {
            return 0L;
        }
        return capabilityCache
            .getItemHandler()
            .map(itemHandler -> {
                final long correctedAmount = amountOverride.overrideAmount(
                    resource,
                    amount,
                    () -> ForgeHandlerUtil.getCurrentAmount(itemHandler, itemResource.toItemStack())
                );
                if (correctedAmount == 0) {
                    return 0L;
                }
                return doInsert(itemResource, correctedAmount, action, itemHandler);
            })
            .orElse(0L);
    }

    private long doInsert(final ItemResource resource,
                          final long amount,
                          final Action action,
                          final IItemHandler itemHandler) {
        final ItemStack stack = resource.toItemStack(amount);
        final ItemStack remainder = ItemHandlerHelper.insertItem(
            itemHandler,
            stack,
            action == Action.SIMULATE
        );
        return amount - remainder.getCount();
    }
}
