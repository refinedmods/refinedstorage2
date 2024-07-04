package com.refinedmods.refinedstorage.platform.common.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class ResourceCodecs {
    public static final MapCodec<ItemResource> ITEM_MAP_CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemResource::item),
        DataComponentPatch.CODEC.fieldOf("components").forGetter(ItemResource::components)
    ).apply(ins, ItemResource::new));
    public static final Codec<ItemResource> ITEM_CODEC = ITEM_MAP_CODEC.codec();

    public static final MapCodec<FluidResource> FLUID_MAP_CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidResource::fluid),
        DataComponentPatch.CODEC.fieldOf("components").forGetter(FluidResource::components)
    ).apply(ins, FluidResource::new));
    public static final Codec<FluidResource> FLUID_CODEC = FLUID_MAP_CODEC.codec();

    public static final Codec<PlatformResourceKey> CODEC = PlatformApi.INSTANCE.getResourceTypeRegistry()
        .codec()
        .dispatch(PlatformResourceKey::getResourceType, ResourceType::getMapCodec);
    public static final Codec<ResourceAmount> AMOUNT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CODEC.fieldOf("resource").forGetter(resourceAmount -> (PlatformResourceKey) resourceAmount.getResource()),
        Codec.LONG.fieldOf("amount").forGetter(ResourceAmount::getAmount)
    ).apply(instance, ResourceAmount::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlatformResourceKey decode(final RegistryFriendlyByteBuf buf) {
            final ResourceLocation id = buf.readResourceLocation();
            final ResourceType resourceType = PlatformApi.INSTANCE.getResourceTypeRegistry().get(id).orElseThrow();
            return resourceType.getStreamCodec().decode(buf);
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf buf, final PlatformResourceKey resourceKey) {
            final ResourceType resourceType = resourceKey.getResourceType();
            final ResourceLocation id = PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType)
                .orElseThrow();
            buf.writeResourceLocation(id);
            resourceType.getStreamCodec().encode(buf, resourceKey);
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceAmount> AMOUNT_STREAM_CODEC = StreamCodec.of(
        (buf, resourceAmount) -> {
            final ResourceKey resourceKey = resourceAmount.getResource();
            if (!(resourceKey instanceof PlatformResourceKey platformResourceKey)) {
                throw new DecoderException("Cannot encode non-platform resource key");
            }
            STREAM_CODEC.encode(buf, platformResourceKey);
            buf.writeLong(resourceAmount.getAmount());
        },
        buf -> {
            final PlatformResourceKey resourceKey = STREAM_CODEC.decode(buf);
            final long amount = buf.readLong();
            return new ResourceAmount(resourceKey, amount);
        }
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<ResourceAmount>> AMOUNT_STREAM_OPTIONAL_CODEC =
        ByteBufCodecs.optional(AMOUNT_STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemResource> ITEM_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(Registries.ITEM), ItemResource::item,
        DataComponentPatch.STREAM_CODEC, ItemResource::components,
        ItemResource::new
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidResource> FLUID_STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.registry(Registries.FLUID), FluidResource::fluid,
        DataComponentPatch.STREAM_CODEC, FluidResource::components,
        FluidResource::new
    );

    private ResourceCodecs() {
    }
}
