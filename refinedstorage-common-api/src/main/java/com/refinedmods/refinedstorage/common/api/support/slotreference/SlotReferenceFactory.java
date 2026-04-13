package com.refinedmods.refinedstorage.common.api.support.slotreference;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
@FunctionalInterface
public interface SlotReferenceFactory {
    StreamCodec<RegistryFriendlyByteBuf, SlotReference> STREAM_CODEC = StreamCodec.of(
        (buf, slotReference) -> {
            final Identifier factoryId = RefinedStorageApi.INSTANCE.getSlotReferenceFactoryRegistry()
                .getId(slotReference.getFactory())
                .orElseThrow();
            buf.writeIdentifier(factoryId);
            slotReference.getFactory().getStreamCodec().encode(buf, slotReference);
        },
        buf -> {
            final Identifier factoryId = buf.readIdentifier();
            final SlotReferenceFactory factory = RefinedStorageApi.INSTANCE.getSlotReferenceFactoryRegistry()
                .get(factoryId)
                .orElseThrow();
            return factory.getStreamCodec().decode(buf);
        }
    );

    StreamCodec<RegistryFriendlyByteBuf, SlotReference> getStreamCodec();
}
