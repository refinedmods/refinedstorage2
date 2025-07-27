package com.refinedmods.refinedstorage.common.support.packet.s2c;

import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewType;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.TreePreviewNode;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.support.resource.ResourceCodecs;
import com.refinedmods.refinedstorage.common.util.ClientPlatformUtil;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.PlatformUtil.enumStreamCodec;

public record AutocraftingTreePreviewResponsePacket(UUID id, TreePreview preview) implements CustomPacketPayload {
    public static final Type<AutocraftingTreePreviewResponsePacket> PACKET_TYPE = new Type<>(
        createIdentifier("autocrafting_tree_preview_response")
    );
    private static final StreamCodec<RegistryFriendlyByteBuf, TreePreviewNode> PREVIEW_NODE_STREAM_CODEC =
        StreamCodec.recursive(codec -> StreamCodec.composite(
            ResourceCodecs.STREAM_CODEC, node -> (PlatformResourceKey) node.getResource(),
            ByteBufCodecs.VAR_LONG, TreePreviewNode::getAmount,
            ByteBufCodecs.VAR_LONG, TreePreviewNode::getAvailable,
            ByteBufCodecs.VAR_LONG, TreePreviewNode::getMissing,
            ByteBufCodecs.collection(ArrayList::new, codec), TreePreviewNode::getChildren,
            TreePreviewNode::new
        ));
    private static final StreamCodec<RegistryFriendlyByteBuf, TreePreview> PREVIEW_STREAM_CODEC =
        StreamCodec.composite(
            enumStreamCodec(PreviewType.values()), TreePreview::type,
            PREVIEW_NODE_STREAM_CODEC, TreePreview::rootNode,
            ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.AMOUNT_STREAM_CODEC),
            TreePreview::outputsOfPatternWithCycle,
            TreePreview::new
        );
    public static final StreamCodec<RegistryFriendlyByteBuf, AutocraftingTreePreviewResponsePacket> STREAM_CODEC =
        StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, AutocraftingTreePreviewResponsePacket::id,
            PREVIEW_STREAM_CODEC, AutocraftingTreePreviewResponsePacket::preview,
            AutocraftingTreePreviewResponsePacket::new
        );

    public static void handle(final AutocraftingTreePreviewResponsePacket packet) {
        ClientPlatformUtil.autocraftingPreviewResponseReceived(packet.id, packet.preview);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}

