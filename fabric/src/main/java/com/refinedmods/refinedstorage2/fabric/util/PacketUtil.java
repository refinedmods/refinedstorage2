package com.refinedmods.refinedstorage2.fabric.util;

import java.util.Optional;
import java.util.function.Consumer;

import com.refinedmods.refinedstorage2.core.storage.StorageTracker;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PacketUtil {
    private PacketUtil() {
    }

    public static void sendToServer(Identifier id, Consumer<PacketByteBuf> bufConsumer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ClientPlayNetworking.send(id, buf);
    }

    public static void sendToPlayer(ServerPlayerEntity playerEntity, Identifier id, Consumer<PacketByteBuf> bufConsumer) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        bufConsumer.accept(buf);
        ServerPlayNetworking.send(playerEntity, id, buf);
    }

    public static void writeItemStackWithoutCount(PacketByteBuf buf, ItemStack stack) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            Item item = stack.getItem();
            buf.writeVarInt(Item.getRawId(item));

            CompoundTag compoundTag = null;
            if (item.isDamageable() || item.shouldSyncTagToClient()) {
                compoundTag = stack.getTag();
            }

            buf.writeCompoundTag(compoundTag);
        }
    }

    public static ItemStack readItemStackWithoutCount(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return ItemStack.EMPTY;
        } else {
            int id = buf.readVarInt();
            ItemStack itemStack = new ItemStack(Item.byRawId(id), 1);
            itemStack.setTag(buf.readCompoundTag());
            return itemStack;
        }
    }

    public static void writeTrackerEntry(PacketByteBuf buf, Optional<StorageTracker.Entry> entry) {
        if (!entry.isPresent()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(entry.get().getTime());
            buf.writeString(entry.get().getName());
        }
    }

    public static StorageTracker.Entry readTrackerEntry(PacketByteBuf buf) {
        if (!buf.readBoolean()) {
            return null;
        }
        long time = buf.readLong();
        String name = buf.readString(32767);
        return new StorageTracker.Entry(time, name);
    }
}
