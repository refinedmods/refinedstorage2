package com.refinedmods.refinedstorage.common.support.resource;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainerContents;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;

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
import net.minecraft.resources.Identifier;

public final class ResourceCodecs {
    public static final MapCodec<ItemResource> ITEM_MAP_CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
        BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(ItemResource::item),
        DataComponentPatch.CODEC.fieldOf("components").forGetter(ItemResource::components)
    ).apply(ins, ItemResource::new));
    public static final Codec<ItemResource> ITEM_CODEC = ITEM_MAP_CODEC.codec();
    public static final Codec<ResourceKey> NATIVE_ITEM_CODEC = ITEM_CODEC.xmap(
        itemResource -> itemResource,
        resourceKey -> {
            if (resourceKey instanceof ItemResource itemResource) {
                return itemResource;
            }
            throw new IllegalArgumentException("Expected ItemResource");
        }
    );

    public static final MapCodec<FluidResource> FLUID_MAP_CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
        BuiltInRegistries.FLUID.byNameCodec().fieldOf("fluid").forGetter(FluidResource::fluid),
        DataComponentPatch.CODEC.fieldOf("components").forGetter(FluidResource::components)
    ).apply(ins, FluidResource::new));
    public static final Codec<FluidResource> FLUID_CODEC = FLUID_MAP_CODEC.codec();
    public static final Codec<ResourceKey> NATIVE_FLUID_CODEC = FLUID_CODEC.xmap(
        fluidResource -> fluidResource,
        resourceKey -> {
            if (resourceKey instanceof FluidResource fluidResource) {
                return fluidResource;
            }
            throw new IllegalArgumentException("Expected FluidResource");
        }
    );

    public static final Codec<PlatformResourceKey> CODEC = RefinedStorageApi.INSTANCE.getResourceTypeRegistry()
        .codec()
        .dispatch(PlatformResourceKey::getResourceType, ResourceType::getMapCodec);
    public static final Codec<ResourceKey> NATIVE_CODEC = RefinedStorageApi.INSTANCE.getResourceTypeRegistry()
        .codec()
        .dispatch(r -> ((PlatformResourceKey) r).getResourceType(), ResourceType::getMapCodec);

    public static final Codec<ResourceAmount> AMOUNT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
        CODEC.fieldOf("resource").forGetter(resourceAmount -> (PlatformResourceKey) resourceAmount.resource()),
        Codec.LONG.fieldOf("amount").forGetter(ResourceAmount::amount)
    ).apply(instance, ResourceAmount::new));
    public static final Codec<Optional<ResourceAmount>> AMOUNT_OPTIONAL_CODEC = AMOUNT_CODEC.optionalFieldOf("resource")
        .codec();

    public static final Codec<ResourceContainerContents> CONTAINER_CONTENTS_CODEC =
        Codec.list(ResourceCodecs.AMOUNT_OPTIONAL_CODEC)
            .xmap(ResourceContainerContents::new, ResourceContainerContents::slots);

    public static final StreamCodec<RegistryFriendlyByteBuf, PlatformResourceKey> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public PlatformResourceKey decode(final RegistryFriendlyByteBuf buf) {
            final Identifier id = buf.readIdentifier();
            final ResourceType resourceType = RefinedStorageApi.INSTANCE.getResourceTypeRegistry()
                .get(id)
                .orElseThrow();
            return resourceType.getStreamCodec().decode(buf);
        }

        @Override
        public void encode(final RegistryFriendlyByteBuf buf, final PlatformResourceKey resourceKey) {
            final ResourceType resourceType = resourceKey.getResourceType();
            final Identifier id = RefinedStorageApi.INSTANCE.getResourceTypeRegistry().getId(resourceType)
                .orElseThrow();
            buf.writeIdentifier(id);
            resourceType.getStreamCodec().encode(buf, resourceKey);
        }
    };
    public static final StreamCodec<RegistryFriendlyByteBuf, ResourceAmount> AMOUNT_STREAM_CODEC = StreamCodec.of(
        (buf, resourceAmount) -> {
            final ResourceKey resourceKey = resourceAmount.resource();
            if (!(resourceKey instanceof PlatformResourceKey platformResourceKey)) {
                throw new DecoderException("Cannot encode non-platform resource key");
            }
            STREAM_CODEC.encode(buf, platformResourceKey);
            buf.writeLong(resourceAmount.amount());
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
