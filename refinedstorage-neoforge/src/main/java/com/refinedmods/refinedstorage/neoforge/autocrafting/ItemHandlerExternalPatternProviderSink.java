package com.refinedmods.refinedstorage.neoforge.autocrafting;

import com.refinedmods.refinedstorage.api.autocrafting.task.ExternalPatternSink;
import com.refinedmods.refinedstorage.api.core.Action;
import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.common.api.autocrafting.PlatformPatternProviderExternalPatternSink;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.neoforge.storage.CapabilityCache;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ItemHandlerExternalPatternProviderSink implements PlatformPatternProviderExternalPatternSink {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemHandlerExternalPatternProviderSink.class);

    private final CapabilityCache capabilityCache;

    ItemHandlerExternalPatternProviderSink(final CapabilityCache capabilityCache) {
        this.capabilityCache = capabilityCache;
    }

    @Override
    public ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources, final Action action) {
        return capabilityCache.getItemHandler()
            .map(handler -> accept(resources, action, handler))
            .orElse(ExternalPatternSink.Result.SKIPPED);
    }

    private ExternalPatternSink.Result accept(final Collection<ResourceAmount> resources,
                                              final Action action,
                                              final IItemHandler handler) {
        final Deque<ItemStack> stacks = getStacks(resources);
        ItemStack current = stacks.poll();
        final List<Integer> availableSlots = IntStream.range(0, handler.getSlots())
            .boxed()
            .collect(Collectors.toList());
        while (current != null && !availableSlots.isEmpty()) {
            final ItemStack remainder = insert(action, handler, availableSlots, current);
            if (remainder.isEmpty()) {
                current = stacks.poll();
            } else if (current.getCount() == remainder.getCount()) {
                break;
            } else {
                current = remainder;
            }
        }
        final boolean success = current == null && stacks.isEmpty();
        if (!success && action == Action.EXECUTE) {
            LOGGER.warn(
                "{} unexpectedly left {} as a remainder, which has been voided",
                handler,
                stacks
            );
        }
        return success ? ExternalPatternSink.Result.ACCEPTED : ExternalPatternSink.Result.REJECTED;
    }

    private ItemStack insert(final Action action,
                             final IItemHandler handler,
                             final List<Integer> availableSlots,
                             final ItemStack current) {
        ItemStack remainder = ItemStack.EMPTY;
        for (int i = 0; i < availableSlots.size(); ++i) {
            final int slot = availableSlots.get(i);
            remainder = handler.insertItem(slot, current.copy(), action == Action.SIMULATE);
            if (remainder.isEmpty() || current.getCount() != remainder.getCount()) {
                availableSlots.remove(i);
                break;
            }
        }
        return remainder;
    }

    private static ArrayDeque<ItemStack> getStacks(final Collection<ResourceAmount> resources) {
        // Create a new ArrayList to preserve the original order from the pattern.
        // A for-loop is used here instead of stream processing to ensure clarity and 
        // explicitly preserve the order of resources as they are processed.
        List<ItemStack> orderedStacks = new ArrayList<>();
        
        for (ResourceAmount resourceAmount : resources) {
            if (resourceAmount.resource() instanceof ItemResource) {
                final ItemResource itemResource = (ItemResource) resourceAmount.resource();
                orderedStacks.add(itemResource.toItemStack(resourceAmount.amount()));
            }
        }
        
        return new ArrayDeque<>(orderedStacks);
    }

    @Override
    public boolean isEmpty() {
        return capabilityCache.getItemHandler().map(handler -> {
            for (int i = 0; i < handler.getSlots(); ++i) {
                if (!handler.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }).orElse(true);
    }
}
