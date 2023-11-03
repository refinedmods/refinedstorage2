package com.refinedmods.refinedstorage2.platform.common.networking;

import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

class NetworkCardInventory extends SimpleContainer {
    NetworkCardInventory() {
        super(1);
    }

    @Override
    public boolean canPlaceItem(final int slot, final ItemStack stack) {
        return stack.getItem() instanceof NetworkCardItem networkCardItem && networkCardItem.isActive(stack);
    }

    ItemStack getNetworkCard() {
        return getItem(0);
    }

    Optional<GlobalPos> getReceiverLocation() {
        final ItemStack stack = getNetworkCard();
        if (stack.getItem() instanceof NetworkCardItem cardItem) {
            return cardItem.getLocation(stack);
        }
        return Optional.empty();
    }
}
