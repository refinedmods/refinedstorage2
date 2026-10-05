package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class PlatformNetworkNodeDetails extends AbstractNetworkNodeDetails {
    public static final StreamCodec<RegistryFriendlyByteBuf, PlatformNetworkNodeDetails> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.LONG, PlatformNetworkNodeDetails::getEnergyUsage,
            ByteBufCodecs.BOOL, PlatformNetworkNodeDetails::isActive,
            ByteBufCodecs.collection(ArrayList::new, StreamCodec.of(
                PlatformNetworkNodeDetails::encodeElement,
                PlatformNetworkNodeDetails::decodeElement
            )),
            PlatformNetworkNodeDetails::getElements,
            PlatformNetworkNodeDetails::new
        );

    private final List<NetworkNodeDetailsElement> elements;

    public PlatformNetworkNodeDetails(final long energyUsage,
                                      final boolean active,
                                      final List<NetworkNodeDetailsElement> elements) {
        super(energyUsage, active);
        this.elements = List.copyOf(elements);
    }

    public List<NetworkNodeDetailsElement> getElements() {
        return elements;
    }

    private static void encodeElement(final RegistryFriendlyByteBuf buf, final NetworkNodeDetailsElement element) {
        switch (element) {
            case NetworkNodeDetailsElement.Text text -> {
                buf.writeVarInt(0);
                NetworkNodeDetailsElement.Text.STREAM_CODEC.encode(buf, text);
            }
            case NetworkNodeDetailsElement.Resources resources -> {
                buf.writeVarInt(1);
                NetworkNodeDetailsElement.Resources.STREAM_CODEC.encode(buf, resources);
            }
            case NetworkNodeDetailsElement.Items items -> {
                buf.writeVarInt(2);
                NetworkNodeDetailsElement.Items.STREAM_CODEC.encode(buf, items);
            }
            case NetworkNodeDetailsElement.ResourceAmounts resourceAmounts -> {
                buf.writeVarInt(3);
                NetworkNodeDetailsElement.ResourceAmounts.STREAM_CODEC.encode(buf, resourceAmounts);
            }
            case NetworkNodeDetailsElement.Progress progress -> {
                buf.writeVarInt(4);
                NetworkNodeDetailsElement.Progress.STREAM_CODEC.encode(buf, progress);
            }
        }
    }

    private static NetworkNodeDetailsElement decodeElement(final RegistryFriendlyByteBuf buf) {
        return switch (buf.readVarInt()) {
            case 0 -> NetworkNodeDetailsElement.Text.STREAM_CODEC.decode(buf);
            case 1 -> NetworkNodeDetailsElement.Resources.STREAM_CODEC.decode(buf);
            case 2 -> NetworkNodeDetailsElement.Items.STREAM_CODEC.decode(buf);
            case 3 -> NetworkNodeDetailsElement.ResourceAmounts.STREAM_CODEC.decode(buf);
            case 4 -> NetworkNodeDetailsElement.Progress.STREAM_CODEC.decode(buf);
            default -> throw new IllegalArgumentException("Unknown network node details element");
        };
    }
}
