package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ItemResource implements FuzzyModeNormalizer<ItemResource> {
    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";

    private final Item item;
    private final CompoundTag tag;

    public ItemResource(Item item, CompoundTag tag) {
        this.item = item;
        this.tag = tag;
    }

    public ItemResource(ItemStack stack) {
        this(stack.getItem(), stack.getTag());
    }

    public static CompoundTag toTag(ItemResource itemResource) {
        CompoundTag tag = new CompoundTag();
        if (itemResource.getTag() != null) {
            tag.put(TAG_TAG, itemResource.getTag());
        }
        tag.putString(TAG_ID, Registry.ITEM.getKey(itemResource.getItem()).toString());
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

    public Item getItem() {
        return item;
    }

    public CompoundTag getTag() {
        return tag;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemResource that = (ItemResource) o;
        return Objects.equals(item, that.item) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }

    @Override
    public String toString() {
        return "ItemResource{" +
                "item=" + item +
                ", tag=" + tag +
                '}';
    }
}
