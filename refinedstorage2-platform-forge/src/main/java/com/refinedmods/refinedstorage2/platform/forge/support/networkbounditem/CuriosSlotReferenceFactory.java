package com.refinedmods.refinedstorage2.platform.forge.support.networkbounditem;

import com.refinedmods.refinedstorage2.platform.api.support.networkbounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.networkbounditem.SlotReferenceFactory;

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
