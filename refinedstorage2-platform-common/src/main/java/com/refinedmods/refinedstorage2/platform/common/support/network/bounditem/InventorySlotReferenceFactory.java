package com.refinedmods.refinedstorage2.platform.common.support.network.bounditem;

import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReferenceFactory;

import net.minecraft.network.FriendlyByteBuf;

public class InventorySlotReferenceFactory implements SlotReferenceFactory {
    public static final SlotReferenceFactory INSTANCE = new InventorySlotReferenceFactory();

    private InventorySlotReferenceFactory() {
    }

    @Override
    public SlotReference create(final FriendlyByteBuf buf) {
        return new InventorySlotReference(buf.readInt());
    }
}
