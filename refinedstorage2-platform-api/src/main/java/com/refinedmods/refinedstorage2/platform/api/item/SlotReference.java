package com.refinedmods.refinedstorage2.platform.api.item;

import java.util.Optional;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface SlotReference {
    boolean isDisabledSlot(int playerSlotIndex);

    void writeToBuf(ByteBuf buf);

    Optional<ItemStack> resolve(Player player);
}
