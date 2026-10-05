package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public sealed interface NetworkNodeDetailsElement {
    record Text(Component text, Optional<Component> rightText) implements NetworkNodeDetailsElement {
        static final StreamCodec<RegistryFriendlyByteBuf, Text> STREAM_CODEC = StreamCodec.composite(
            ComponentSerialization.STREAM_CODEC, Text::text,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), Text::rightText,
            Text::new
        );

        public Text(final Component text) {
            this(text, Optional.empty());
        }

        public Text(final Component text, final Component rightText) {
            this(text, Optional.of(rightText));
        }
    }

    record Resources(List<Optional<ResourceKey>> resources) implements NetworkNodeDetailsElement {
        static final StreamCodec<RegistryFriendlyByteBuf, Resources> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                ArrayList::new,
                ByteBufCodecs.optional(ResourceCodecs.STREAM_CODEC.map(
                    resource -> resource,
                    resource -> (PlatformResourceKey) resource
                ))
            ), Resources::resources,
            Resources::new
        );

        public Resources {
            resources = List.copyOf(resources);
        }
    }

    record ResourceAmounts(List<Optional<ResourceAmount>> resources) implements NetworkNodeDetailsElement {
        static final StreamCodec<RegistryFriendlyByteBuf, ResourceAmounts> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.optional(ResourceCodecs.AMOUNT_STREAM_CODEC)),
            ResourceAmounts::resources,
            ResourceAmounts::new
        );

        public ResourceAmounts {
            resources = List.copyOf(resources);
        }
    }

    record Progress(long stored, long capacity) implements NetworkNodeDetailsElement {
        static final StreamCodec<RegistryFriendlyByteBuf, Progress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.LONG, Progress::stored,
            ByteBufCodecs.LONG, Progress::capacity,
            Progress::new
        );

        public double getProgress() {
            if (capacity <= 0) {
                return 0;
            }
            return (double) stored / (double) capacity;
        }
    }

    record Items(List<ItemStack> items) implements NetworkNodeDetailsElement {
        static final StreamCodec<RegistryFriendlyByteBuf, Items> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ItemStack.OPTIONAL_STREAM_CODEC), Items::items,
            Items::new
        );

        public Items {
            items = items.stream().map(ItemStack::copy).toList();
        }
    }
}
