package com.refinedmods.refinedstorage2.platform.forge.internal.storage;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.storage.Actor;
import com.refinedmods.refinedstorage2.api.storage.InsertableStorage;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.AmountOverride;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemHandlerInsertableStorage implements InsertableStorage<ItemResource> {
    private final InteractionCoordinates interactionCoordinates;
    private final AmountOverride amountOverride;

    public ItemHandlerInsertableStorage(final InteractionCoordinates interactionCoordinates,
                                        final AmountOverride amountOverride) {
        this.interactionCoordinates = interactionCoordinates;
        this.amountOverride = amountOverride;
    }

    @Override
    public long insert(final ItemResource resource, final long amount, final Action action, final Actor actor) {
        return interactionCoordinates
            .getItemHandler()
            .map(itemHandler -> {
                final long correctedAmount = amountOverride.overrideAmount(
                    resource,
                    amount,
                    () -> HandlerUtil.getCurrentAmount(itemHandler, resource.toItemStack())
                );
                if (correctedAmount == 0) {
                    return 0L;
                }
                return doInsert(resource, correctedAmount, action, itemHandler);
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
