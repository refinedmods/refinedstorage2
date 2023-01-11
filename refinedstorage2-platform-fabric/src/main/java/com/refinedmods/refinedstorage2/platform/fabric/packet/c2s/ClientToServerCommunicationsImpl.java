package com.refinedmods.refinedstorage2.platform.fabric.packet.c2s;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyType;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridScrollModeUtil;
import com.refinedmods.refinedstorage2.platform.common.packet.ClientToServerCommunications;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;

import java.util.UUID;
import java.util.function.Consumer;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class ClientToServerCommunicationsImpl implements ClientToServerCommunications {
    @Override
    public void sendGridItemExtract(final ItemResource itemResource, final GridExtractMode mode, final boolean cursor) {
        sendToServer(PacketIds.GRID_EXTRACT, buf -> {
            GridExtractPacket.writeMode(buf, mode);
            buf.writeBoolean(cursor);
            PacketUtil.writeItemResource(buf, itemResource);
        });
    }

    @Override
    public void sendGridFluidExtract(final FluidResource fluidResource,
                                     final GridExtractMode mode,
                                     final boolean cursor) {
        sendToServer(PacketIds.GRID_EXTRACT, buf -> {
            GridExtractPacket.writeMode(buf, mode);
            buf.writeBoolean(cursor);
            PacketUtil.writeFluidResource(buf, fluidResource);
        });
    }

    @Override
    public void sendGridInsert(final GridInsertMode mode) {
        sendToServer(PacketIds.GRID_INSERT, buf -> buf.writeBoolean(mode == GridInsertMode.SINGLE_RESOURCE));
    }

    @Override
    public void sendGridScroll(final ItemResource itemResource, final GridScrollMode mode, final int slotIndex) {
        sendToServer(PacketIds.GRID_SCROLL, buf -> {
            PacketUtil.writeItemResource(buf, itemResource);
            GridScrollModeUtil.writeMode(buf, mode);
            buf.writeInt(slotIndex);
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
    public void sendResourceTypeChange(final ResourceType type) {
        PlatformApi.INSTANCE.getResourceTypeRegistry()
            .getId(type)
            .ifPresent(id -> sendToServer(PacketIds.RESOURCE_TYPE_CHANGE, buf -> buf.writeResourceLocation(id)));
    }

    @Override
    public void sendStorageInfoRequest(final UUID storageId) {
        sendToServer(PacketIds.STORAGE_INFO_REQUEST, buf -> buf.writeUUID(storageId));
    }

    @Override
    public void sendResourceFilterSlotAmountChange(final int slotIndex, final long amount) {
        sendToServer(PacketIds.RESOURCE_FILTER_SLOT_AMOUNT_CHANGE, buf -> {
            buf.writeInt(slotIndex);
            buf.writeLong(amount);
        });
    }

    private static void sendToServer(final ResourceLocation id, final Consumer<FriendlyByteBuf> bufConsumer) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ClientPlayNetworking.send(id, buf);
    }
}
