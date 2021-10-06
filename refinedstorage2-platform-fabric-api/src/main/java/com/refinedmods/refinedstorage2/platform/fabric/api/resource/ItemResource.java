package com.refinedmods.refinedstorage2.platform.fabric.api.resource;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Objects;
import java.util.Optional;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public final class ItemResource {
    private final Item item;
    private final NbtCompound tag;
    private final ItemStack itemStack;

    public ItemResource(Item item, NbtCompound tag) {
        this.item = item;
        this.tag = tag;
        this.itemStack = new ItemStack(item);
        this.itemStack.setNbt(tag);
    }

    public ItemResource(ItemStack stack) {
        this(stack.getItem(), stack.getNbt());
    }

    public Item getItem() {
        return item;
    }

    public NbtCompound getTag() {
        return tag;
    }

    public ItemStack getItemStack() {
        return itemStack;
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

    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";

    public static NbtCompound toTag(ItemResource itemResource) {
        NbtCompound tag = new NbtCompound();
        if (itemResource.getTag() != null) {
            tag.put(TAG_TAG, itemResource.getTag());
        }
        tag.putString(TAG_ID, Registry.ITEM.getId(itemResource.getItem()).toString());
        return tag;
    }

    public static NbtCompound toTagWithAmount(ResourceAmount<ItemResource> resourceAmount) {
        NbtCompound tag = toTag(resourceAmount.getResource());
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        return tag;
    }

    public static Optional<ItemResource> fromTag(NbtCompound tag) {
        Identifier id = new Identifier(tag.getString(TAG_ID));
        Item item = Registry.ITEM.get(id);
        if (item == Items.AIR) {
            return Optional.empty();
        }
        NbtCompound itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new ItemResource(item, itemTag));
    }

    public static Optional<ResourceAmount<ItemResource>> fromTagWithAmount(NbtCompound tag) {
        return fromTag(tag).map(itemResource -> new ResourceAmount<>(itemResource, tag.getLong(TAG_AMOUNT)));
    }
}
