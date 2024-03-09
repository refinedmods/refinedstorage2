package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

abstract class AbstractResourceContainerContainerAdapter implements Container {
    private final ResourceContainer container;

    protected AbstractResourceContainerContainerAdapter(final ResourceContainer container) {
        this.container = container;
    }

    @Override
    public int getContainerSize() {
        return container.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < container.size(); ++i) {
            if (!container.isEmpty(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(final int slotIndex) {
        return container.getStackRepresentation(slotIndex);
    }

    @Override
    public ItemStack removeItem(final int slotIndex, final int amount) {
        final ResourceKey resource = container.getResource(slotIndex);
        if (resource instanceof ItemResource itemResource) {
            final long maxRemove = Math.min(amount, container.getAmount(slotIndex));
            container.shrink(slotIndex, maxRemove);
            return itemResource.toItemStack(maxRemove);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slotIndex) {
        final ResourceKey resource = container.getResource(slotIndex);
        if (resource instanceof ItemResource itemResource) {
            final ItemStack stack = itemResource.toItemStack();
            final long maxRemove = Math.min(stack.getMaxStackSize(), container.getAmount(slotIndex));
            container.shrink(slotIndex, maxRemove);
            return stack.copyWithCount((int) maxRemove);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return container.isEmpty(slot);
    }

    @Override
    public void setItem(final int slotIndex, final ItemStack itemStack) {
        final ResourceKey resource = container.getResource(slotIndex);
        if (itemStack.isEmpty()) {
            if (resource instanceof ItemResource) {
                container.remove(slotIndex);
            }
            return;
        }
        container.set(slotIndex, new ResourceAmount(
            ItemResource.ofItemStack(itemStack),
            itemStack.getCount()
        ));
    }

    @Override
    public boolean stillValid(final Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < container.size(); ++i) {
            container.remove(i);
        }
    }
}
