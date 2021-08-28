package com.refinedmods.refinedstorage2.platform.fabric.api.util;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;

import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class FluidStacks {
    private static final String TAG_AMOUNT = "amount";
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";

    private FluidStacks() {
    }

    public static NbtCompound toTag(Rs2FluidStack stack) {
        NbtCompound tag = new NbtCompound();
        tag.putLong(TAG_AMOUNT, stack.getAmount());
        if (stack.getTag() != null) {
            tag.put(TAG_TAG, (NbtCompound) stack.getTag());
        }
        tag.putString(TAG_ID, stack.getFluid().getIdentifier());
        return tag;
    }

    public static Rs2FluidStack fromTag(NbtCompound tag) {
        Identifier id = new Identifier(tag.getString(TAG_ID));
        Fluid fluid = Registry.FLUID.get(id);
        if (fluid == Fluids.EMPTY) {
            return Rs2FluidStack.EMPTY;
        }
        long amount = tag.getLong(TAG_AMOUNT);
        NbtCompound stackTag = tag.getCompound(TAG_TAG);
        return new Rs2FluidStack(Rs2PlatformApiFacade.INSTANCE.toRs2Fluid(FluidVariantImpl.of(fluid, stackTag)), amount, stackTag);
    }
}
