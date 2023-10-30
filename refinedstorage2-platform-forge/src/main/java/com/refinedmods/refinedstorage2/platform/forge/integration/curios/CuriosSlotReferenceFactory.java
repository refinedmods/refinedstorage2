package com.refinedmods.refinedstorage2.platform.forge.integration.curios;

import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.item.SlotReferenceFactory;

import net.minecraft.network.FriendlyByteBuf;

public class CuriosSlotReferenceFactory implements SlotReferenceFactory {
    public static final SlotReferenceFactory INSTANCE = new CuriosSlotReferenceFactory();

    private CuriosSlotReferenceFactory() {
    }

    @Override
    public SlotReference create(final FriendlyByteBuf buf) {
        return new CuriosSlotReference(buf.readUtf(), buf.readInt());
    }
}
