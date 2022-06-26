package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.fluid;

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
    private static final MutableComponent NAME = createTranslation("misc", "resource_type.fluid");

    public static final FluidResourceType INSTANCE = new FluidResourceType();

    private FluidResourceType() {
    }

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Optional<FilteredResource> translate(ItemStack stack) {
        return Platform.INSTANCE.convertToFluid(stack).map(FluidFilteredResource::new);
    }

    @Override
    public FilteredResource fromPacket(FriendlyByteBuf buf) {
        return new FluidFilteredResource(PacketUtil.readFluidResource(buf));
    }

    @Override
    public Optional<FilteredResource> fromTag(CompoundTag tag) {
        return FluidResource.fromTag(tag).map(FluidFilteredResource::new);
    }
}
