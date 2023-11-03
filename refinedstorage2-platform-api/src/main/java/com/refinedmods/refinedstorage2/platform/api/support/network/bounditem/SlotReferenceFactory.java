package com.refinedmods.refinedstorage2.platform.api.support.network.bounditem;

import net.minecraft.network.FriendlyByteBuf;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface SlotReferenceFactory {
    SlotReference create(FriendlyByteBuf buf);
}
