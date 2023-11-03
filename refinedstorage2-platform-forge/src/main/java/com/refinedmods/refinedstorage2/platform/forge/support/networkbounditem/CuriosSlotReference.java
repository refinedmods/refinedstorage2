package com.refinedmods.refinedstorage2.platform.forge.support.networkbounditem;

import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceFactory;

import java.util.Optional;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

public class CuriosSlotReference implements SlotReference {
    private final String identifier;
    private final int index;

    public CuriosSlotReference(final String identifier, final int index) {
        this.identifier = identifier;
        this.index = index;
    }

    @Override
    public boolean isDisabledSlot(final int playerSlotIndex) {
        return false;
    }

    @Override
    public void writeToBuffer(final FriendlyByteBuf buf) {
        buf.writeUtf(identifier);
        buf.writeInt(index);
    }

    @Override
    public Optional<ItemStack> resolve(final Player player) {
        return CuriosApi.getCuriosInventory(player)
            .resolve()
            .flatMap(curiosInventory -> curiosInventory.findCurio(identifier, index))
            .map(SlotResult::stack);
    }

    @Override
    public SlotReferenceFactory getFactory() {
        return CuriosSlotReferenceFactory.INSTANCE;
    }
}
