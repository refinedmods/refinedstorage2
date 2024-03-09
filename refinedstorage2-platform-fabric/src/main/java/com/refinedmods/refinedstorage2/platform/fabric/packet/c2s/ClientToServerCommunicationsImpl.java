package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.support.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.support.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ItemResource;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientToServerCommunicationsImpl implements ClientToServerCommunications {
    @Override
    public void sendGridExtract(final PlatformResourceKey resource, final GridExtractMode mode, final boolean cursor) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresent(id -> sendToServer(
            PacketIds.GRID_EXTRACT,
            buf -> {
                buf.writeResourceLocation(id);
                GridExtractPacket.writeMode(buf, mode);
                buf.writeBoolean(cursor);
                resource.toBuffer(buf);
            }
        ));
    }

    @Override
    public void sendGridScroll(final PlatformResourceKey resource, final GridScrollMode mode, final int slotIndex) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType).ifPresent(id -> sendToServer(
            PacketIds.GRID_SCROLL,
            buf -> {
                buf.writeResourceLocation(id);
                GridScrollPacket.writeMode(buf, mode);
                buf.writeInt(slotIndex);
                resource.toBuffer(buf);
            }
        ));
    }

    @Override
    public void sendGridInsert(final GridInsertMode mode, final boolean tryAlternatives) {
        sendToServer(PacketIds.GRID_INSERT, buf -> {
            buf.writeBoolean(mode == GridInsertMode.SINGLE_RESOURCE);
            buf.writeBoolean(tryAlternatives);
        });
    }

    @Override
    public void sendCraftingGridClear(final boolean toPlayerInventory) {
        sendToServer(PacketIds.CRAFTING_GRID_CLEAR, buf -> buf.writeBoolean(toPlayerInventory));
    }

    @Override
    public void sendCraftingGridRecipeTransfer(final List<List<ItemResource>> recipe) {
        sendToServer(PacketIds.CRAFTING_GRID_RECIPE_TRANSFER, buf -> {
            buf.writeInt(recipe.size());
            for (final List<ItemResource> slotPossibilities : recipe) {
                buf.writeInt(slotPossibilities.size());
                for (final ItemResource slotPossibility : slotPossibilities) {
                    slotPossibility.toBuffer(buf);
                }
            }
        });
    }

    @Override
    public <T> void sendPropertyChange(final PropertyType<T> type, final T value) {
        sendToServer(PacketIds.PROPERTY_CHANGE, buf -> {
            buf.writeResourceLocation(type.id());
            buf.writeInt(type.serializer().apply(value));
        });
    }

    @Override
    public void sendStorageInfoRequest(final UUID storageId) {
        sendToServer(PacketIds.STORAGE_INFO_REQUEST, buf -> buf.writeUUID(storageId));
    }

    @Override
    public void sendResourceSlotChange(final int slotIndex, final boolean tryAlternatives) {
        sendToServer(PacketIds.RESOURCE_SLOT_CHANGE, buf -> {
            buf.writeInt(slotIndex);
            buf.writeBoolean(tryAlternatives);
        });
    }

    @Override
    public void sendResourceFilterSlotChange(final PlatformResourceKey resource, final int slotIndex) {
        final ResourceType resourceType = resource.getResourceType();
        PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType)
            .ifPresent(id -> sendToServer(PacketIds.RESOURCE_FILTER_SLOT_CHANGE, buf -> {
                buf.writeInt(slotIndex);
                buf.writeResourceLocation(id);
                resource.toBuffer(buf);
            }));
    }

    @Override
    public void sendResourceSlotAmountChange(final int slotIndex, final long amount) {
        sendToServer(PacketIds.RESOURCE_SLOT_AMOUNT_CHANGE, buf -> {
            buf.writeInt(slotIndex);
            buf.writeLong(amount);
        });
    }

    @Override
    public void sendSingleAmountChange(final double amount) {
        sendToServer(PacketIds.SINGLE_AMOUNT_CHANGE, buf -> buf.writeDouble(amount));
    }

    @Override
    public void sendUseNetworkBoundItem(final SlotReference slotReference) {
        sendToServer(
            PacketIds.USE_NETWORK_BOUND_ITEM,
            buf -> PlatformApi.INSTANCE.writeSlotReference(slotReference, buf)
        );
    }

    private static void sendToServer(final ResourceLocation id, final Consumer<FriendlyByteBuf> bufConsumer) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
