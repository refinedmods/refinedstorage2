package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.fluid;

import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FluidResourceType implements ResourceType {
    public static final FluidResourceType INSTANCE = new FluidResourceType();

    private static final MutableComponent NAME = createTranslation("misc", "resource_type.fluid");

    private FluidResourceType() {
    }

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Optional<FilteredResource> translate(final ItemStack stack) {
        return Platform.INSTANCE.convertToFluid(stack).map(fluid -> new FluidFilteredResource(
            fluid,
            Platform.INSTANCE.getBucketAmount()
        ));
    }

    @Override
    public FilteredResource fromPacket(final FriendlyByteBuf buf) {
        return new FluidFilteredResource(PacketUtil.readFluidResource(buf), buf.readLong());
    }

    @Override
    public Optional<FilteredResource> fromTag(final CompoundTag tag) {
        return FluidResource.fromTag(tag)
            .map(fluidResource -> new FluidFilteredResource(
                fluidResource,
                FluidFilteredResource.getAmountFromTag(tag)
            ));
    }
}
