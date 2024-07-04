package com.refinedmods.refinedstorage.platform.api.support.network.bounditem;

import com.refinedmods.refinedstorage.platform.api.PlatformApi;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface SlotReferenceFactory {
    StreamCodec<RegistryFriendlyByteBuf, SlotReference> STREAM_CODEC = StreamCodec.of(
        (buf, slotReference) -> {
            final ResourceLocation factoryId = PlatformApi.INSTANCE.getSlotReferenceFactoryRegistry()
                .getId(slotReference.getFactory())
                .orElseThrow();
            buf.writeResourceLocation(factoryId);
            slotReference.getFactory().getStreamCodec().encode(buf, slotReference);
        },
        buf -> {
            final ResourceLocation factoryId = buf.readResourceLocation();
            final SlotReferenceFactory factory = PlatformApi.INSTANCE.getSlotReferenceFactoryRegistry()
                .get(factoryId)
                .orElseThrow();
            return factory.getStreamCodec().decode(buf);
        }
    );

    StreamCodec<RegistryFriendlyByteBuf, SlotReference> getStreamCodec();
}
