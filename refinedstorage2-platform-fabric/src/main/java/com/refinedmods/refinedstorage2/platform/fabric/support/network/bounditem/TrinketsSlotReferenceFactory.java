package com.refinedmods.refinedstorage2.platform.fabric.support.network.bounditem;

import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceFactory;

import net.minecraft.network.FriendlyByteBuf;

public class TrinketsSlotReferenceFactory implements SlotReferenceFactory {
    public static final SlotReferenceFactory INSTANCE = new TrinketsSlotReferenceFactory();

    private TrinketsSlotReferenceFactory() {
    }

    @Override
    public SlotReference create(final FriendlyByteBuf buf) {
        return new TrinketsSlotReference(buf.readUtf(), buf.readUtf(), buf.readInt());
    }
}
