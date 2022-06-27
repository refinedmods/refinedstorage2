package com.refinedmods.refinedstorage2.platform.apiimpl.resource.filter.item;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import java.util.Optional;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ItemResourceType implements ResourceType {
    private static final MutableComponent NAME = createTranslation("misc", "resource_type.item");

    public static final ItemResourceType INSTANCE = new ItemResourceType();

    private ItemResourceType() {
    }

    @Override
    public Component getName() {
        return NAME;
    }

    @Override
    public Optional<FilteredResource> translate(final ItemStack stack) {
        return stack.isEmpty() ? Optional.empty() : Optional.of(new ItemFilteredResource(new ItemResource(stack.getItem(), stack.getTag())));
    }

    @Override
    public FilteredResource fromPacket(final FriendlyByteBuf buf) {
        return new ItemFilteredResource(PacketUtil.readItemResource(buf));
    }

    @Override
    public Optional<FilteredResource> fromTag(final CompoundTag tag) {
        return ItemResource.fromTag(tag).map(ItemFilteredResource::new);
    }
}
