package com.refinedmods.refinedstorage2.platform.common.internal.item;

import com.refinedmods.refinedstorage2.platform.api.item.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.item.SlotReferenceFactory;

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
