package com.refinedmods.refinedstorage2.fabric.util;

import com.refinedmods.refinedstorage2.core.item.Rs2Item;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.storage.StorageTracker;

import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;

public class PacketUtil {
    private PacketUtil() {
    }

    public static void writeItemStack(PacketByteBuf buf, Rs2ItemStack stack, boolean withCount) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);

            Rs2Item item = stack.getItem();
            buf.writeVarInt(item.getId());

            if (withCount) {
                buf.writeLong(stack.getAmount());
            }

            CompoundTag compoundTag = null;
            if (stack.getTag() != null) {
                compoundTag = (CompoundTag) stack.getTag();
            }

            buf.writeCompoundTag(compoundTag);
        }
    }

    public static Rs2ItemStack readItemStack(PacketByteBuf buf, boolean withCount) {
        if (!buf.readBoolean()) {
            return Rs2ItemStack.EMPTY;
        } else {
            int id = buf.readVarInt();
            long amount = 1;

            if (withCount) {
                amount = buf.readLong();
            }

            return new Rs2ItemStack(ItemStacks.ofItem(Item.byRawId(id)), amount, buf.readCompoundTag());
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
