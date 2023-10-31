package com.refinedmods.refinedstorage2.platform.api.support.networkbounditem;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface SlotReference {
    boolean isDisabledSlot(int playerSlotIndex);

    void writeToBuffer(FriendlyByteBuf buf);

    Optional<ItemStack> resolve(Player player);

    SlotReferenceFactory getFactory();
}
