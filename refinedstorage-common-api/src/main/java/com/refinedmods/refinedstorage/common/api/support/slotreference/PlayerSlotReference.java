package com.refinedmods.refinedstorage.common.api.support.slotreference;

import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface PlayerSlotReference {
    @SuppressWarnings("unchecked")
    StreamCodec<RegistryFriendlyByteBuf, PlayerSlotReference> STREAM_CODEC = StreamCodec.of(
        (buf, playerSlotReference) -> {
            final StreamCodec<RegistryFriendlyByteBuf, PlayerSlotReference> codec =
                (StreamCodec<RegistryFriendlyByteBuf, PlayerSlotReference>) playerSlotReference.getStreamCodec();
            final Identifier factoryId = RefinedStorageApi.INSTANCE.getPlayerSlotReferenceFactories()
                .getId(codec)
                .orElseThrow();
            buf.writeIdentifier(factoryId);
            codec.encode(buf, playerSlotReference);
        },
        buf -> {
            final Identifier factoryId = buf.readIdentifier();
            final var streamCodec = RefinedStorageApi.INSTANCE.getPlayerSlotReferenceFactories()
                .get(factoryId)
                .orElseThrow();
            return streamCodec.decode(buf);
        }
    );

    boolean isDisabled(int playerSlotIndex);

    ItemStack get(Player player);

    void set(Player player, ItemStack stack);

    StreamCodec<RegistryFriendlyByteBuf, ? extends PlayerSlotReference> getStreamCodec();
}
