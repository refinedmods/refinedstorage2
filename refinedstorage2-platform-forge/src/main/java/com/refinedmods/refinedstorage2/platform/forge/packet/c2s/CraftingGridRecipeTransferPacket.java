package com.refinedmods.refinedstorage2.platform.forge.packet.c2s;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class CraftingGridRecipeTransferPacket {
    private final List<List<ItemResource>> recipe;

    public CraftingGridRecipeTransferPacket(final List<List<ItemResource>> recipe) {
        this.recipe = recipe;
    }

    public static CraftingGridRecipeTransferPacket decode(final FriendlyByteBuf buf) {
        final int slots = buf.readInt();
        final List<List<ItemResource>> recipe = new ArrayList<>(slots);
        for (int i = 0; i < slots; ++i) {
            final int slotPossibilityCount = buf.readInt();
            final List<ItemResource> slotPossibilities = new ArrayList<>(slotPossibilityCount);
            for (int j = 0; j < slotPossibilityCount; ++j) {
                slotPossibilities.add(PacketUtil.readItemResource(buf));
            }
            recipe.add(slotPossibilities);
        }
        return new CraftingGridRecipeTransferPacket(recipe);
    }

    public static void encode(final CraftingGridRecipeTransferPacket packet, final FriendlyByteBuf buf) {
        buf.writeInt(packet.recipe.size());
        for (final List<ItemResource> slotPossibilities : packet.recipe) {
            buf.writeInt(slotPossibilities.size());
            for (final ItemResource slotPossibility : slotPossibilities) {
                PacketUtil.writeItemResource(buf, slotPossibility);
            }
        }
    }

    public static void handle(final CraftingGridRecipeTransferPacket packet, final Supplier<NetworkEvent.Context> ctx) {
        final ServerPlayer player = ctx.get().getSender();
        if (player != null) {
            ctx.get().enqueueWork(() -> handle(packet, player));
        }
        ctx.get().setPacketHandled(true);
    }

    private static void handle(final CraftingGridRecipeTransferPacket packet, final Player player) {
        if (player.containerMenu instanceof CraftingGridContainerMenu craftingGridContainerMenu) {
            craftingGridContainerMenu.transferRecipe(packet.recipe);
        }
    }
}
