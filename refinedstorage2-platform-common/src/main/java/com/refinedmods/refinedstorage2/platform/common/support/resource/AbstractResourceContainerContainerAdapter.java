package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractResourceContainerContainerAdapter implements Container {
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
            if (container.get(i) != null) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(final int slotIndex) {
        final ResourceAmountTemplate<?> resourceAmount = container.get(slotIndex);
        if (resourceAmount != null) {
            return resourceAmount.getStackRepresentation();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(final int slotIndex, final int amount) {
        final ResourceAmountTemplate<?> resourceAmount = container.get(slotIndex);
        if (resourceAmount != null && resourceAmount.getResource() instanceof ItemResource itemResource) {
            final long maxRemove = Math.min(amount, resourceAmount.getAmount());
            container.shrink(slotIndex, maxRemove);
            return itemResource.toItemStack(maxRemove);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(final int slotIndex) {
        final ResourceAmountTemplate<?> resourceAmount = container.get(slotIndex);
        if (resourceAmount != null && resourceAmount.getResource() instanceof ItemResource itemResource) {
            final ItemStack stack = itemResource.toItemStack();
            final long maxRemove = Math.min(stack.getMaxStackSize(), resourceAmount.getAmount());
            container.shrink(slotIndex, maxRemove);
            return stack.copyWithCount((int) maxRemove);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return container.get(slot) == null;
    }

    @Override
    public void setItem(final int slotIndex, final ItemStack itemStack) {
        final ResourceAmountTemplate<?> resourceAmount = container.get(slotIndex);
        if (itemStack.isEmpty()) {
            if (resourceAmount != null && resourceAmount.getStorageChannelType() == StorageChannelTypes.ITEM) {
                container.remove(slotIndex);
            }
            return;
        }
        container.set(slotIndex, new ResourceAmountTemplate<>(
            ItemResource.ofItemStack(itemStack),
            itemStack.getCount(),
            StorageChannelTypes.ITEM
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
