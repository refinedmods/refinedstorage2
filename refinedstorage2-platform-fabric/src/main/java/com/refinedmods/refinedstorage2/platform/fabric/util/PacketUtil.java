package com.refinedmods.refinedstorage2.platform.fabric.util;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2Fluid;
import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2Item;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import java.util.Optional;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.registry.Registry;

public final class PacketUtil {
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

            NbtCompound compoundTag = null;
            if (stack.getTag() != null) {
                compoundTag = (NbtCompound) stack.getTag();
            }

            buf.writeNbt(compoundTag);
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

            return new Rs2ItemStack(Rs2PlatformApiFacade.INSTANCE.toRs2Item(Item.byRawId(id)), amount, buf.readNbt());
        }
    }

    public static void writeFluidStack(PacketByteBuf buf, Rs2FluidStack stack, boolean withCount) {
        if (stack.isEmpty()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);

            Rs2Fluid fluid = stack.getFluid();
            buf.writeVarInt(fluid.getId());

            if (withCount) {
                buf.writeLong(stack.getAmount());
            }

            NbtCompound compoundTag = null;
            if (stack.getTag() != null) {
                compoundTag = (NbtCompound) stack.getTag();
            }

            buf.writeNbt(compoundTag);
        }
    }

    public static Rs2FluidStack readFluidStack(PacketByteBuf buf, boolean withCount) {
        if (!buf.readBoolean()) {
            return Rs2FluidStack.EMPTY;
        } else {
            int id = buf.readVarInt();
            long amount = 1;

            if (withCount) {
                amount = buf.readLong();
            }

            FluidVariant fluidVariant = FluidVariantImpl.of(Registry.FLUID.get(id), buf.readNbt());

            return new Rs2FluidStack(Rs2PlatformApiFacade.INSTANCE.toRs2Fluid(fluidVariant), amount, fluidVariant.getNbt());
        }
    }

    public static void writeTrackerEntry(PacketByteBuf buf, Optional<StorageTracker.Entry> entry) {
        if (!entry.isPresent()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(entry.get().time());
            buf.writeString(entry.get().name());
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
