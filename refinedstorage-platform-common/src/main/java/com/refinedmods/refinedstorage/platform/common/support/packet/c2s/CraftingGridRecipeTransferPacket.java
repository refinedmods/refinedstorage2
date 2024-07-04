package com.refinedmods.refinedstorage.platform.common.support.packet.c2s;

import com.refinedmods.refinedstorage.platform.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage.platform.common.support.packet.PacketContext;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.ResourceCodecs;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;

public record CraftingGridRecipeTransferPacket(List<List<ItemResource>> recipe) implements CustomPacketPayload {
    public static final Type<CraftingGridRecipeTransferPacket> PACKET_TYPE = new Type<>(
        createIdentifier("crafting_grid_recipe_transfer")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingGridRecipeTransferPacket> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new,
                ByteBufCodecs.collection(ArrayList::new, ResourceCodecs.ITEM_STREAM_CODEC)),
            CraftingGridRecipeTransferPacket::recipe,
            CraftingGridRecipeTransferPacket::new
        );

    public static void handle(final CraftingGridRecipeTransferPacket packet, final PacketContext ctx) {
        if (ctx.getPlayer().containerMenu instanceof CraftingGridContainerMenu craftingGridContainerMenu) {
            craftingGridContainerMenu.transferRecipe(packet.recipe());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_TYPE;
    }
}
