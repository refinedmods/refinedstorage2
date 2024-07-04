package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.platform.common.support.FilteredContainer;

import java.util.Optional;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.ItemStack;

class NetworkCardInventory extends FilteredContainer {
    NetworkCardInventory() {
        super(
            1,
            stack -> stack.getItem() instanceof NetworkCardItem networkCardItem && networkCardItem.isActive(stack)
        );
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
