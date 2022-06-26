package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Optional;

import com.google.common.base.Preconditions;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public record ItemResource(Item item, CompoundTag tag) implements FuzzyModeNormalizer<ItemResource> {
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";

    public ItemResource(@NotNull Item item, CompoundTag tag) {
        this.item = Preconditions.checkNotNull(item);
        this.tag = tag;
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(item);
        itemStack.setTag(tag);
        return itemStack;
    }

    @Override
    public ItemResource normalize() {
        return new ItemResource(item, null);
    }

    public static CompoundTag toTag(ItemResource itemResource) {
        CompoundTag tag = new CompoundTag();
        if (itemResource.tag() != null) {
            tag.put(TAG_TAG, itemResource.tag());
        }
        tag.putString(TAG_ID, Registry.ITEM.getKey(itemResource.item()).toString());
        return tag;
    }

    public static CompoundTag toTagWithAmount(ResourceAmount<ItemResource> resourceAmount) {
        CompoundTag tag = toTag(resourceAmount.getResource());
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        return tag;
    }

    public static Optional<ItemResource> fromTag(CompoundTag tag) {
        ResourceLocation id = new ResourceLocation(tag.getString(TAG_ID));
        Item item = Registry.ITEM.get(id);
        if (item == Items.AIR) {
            return Optional.empty();
        }
        CompoundTag itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new ItemResource(item, itemTag));
    }

    public static Optional<ResourceAmount<ItemResource>> fromTagWithAmount(CompoundTag tag) {
        return fromTag(tag).map(itemResource -> new ResourceAmount<>(itemResource, tag.getLong(TAG_AMOUNT)));
    }
}
