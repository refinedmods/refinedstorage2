package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record ItemResource(Item item, @Nullable CompoundTag tag) implements FuzzyModeNormalizer<ItemResource> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResource.class);

    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";
    private static final String TAG_AMOUNT = "amount";

    public ItemResource(final Item item, @Nullable final CompoundTag tag) {
        this.item = CoreValidations.validateNotNull(item, "Item must not be null");
        this.tag = tag;
    }

    public ItemStack toItemStack() {
        final ItemStack itemStack = new ItemStack(item);
        itemStack.setTag(tag);
        return itemStack;
    }

    public ItemStack toItemStack(final long amount) {
        if (amount > Integer.MAX_VALUE) {
            LOGGER.warn("Truncating too large amount for {} to fit into ItemStack {}", this, amount);
        }
        final ItemStack stack = toItemStack();
        stack.setCount((int) amount);
        return stack;
    }

    // TODO: This is kinda bad for perf. should just do comparisons instead.
    @Override
    public ItemResource normalize() {
        return new ItemResource(item, null);
    }

    public static ItemResource ofItemStack(final ItemStack itemStack) {
        return new ItemResource(itemStack.getItem(), itemStack.getTag());
    }

    @SuppressWarnings("deprecation") // forge deprecates Registry access
    public static CompoundTag toTag(final ItemResource itemResource) {
        final CompoundTag tag = new CompoundTag();
        if (itemResource.tag() != null) {
            tag.put(TAG_TAG, itemResource.tag());
        }
        tag.putString(TAG_ID, BuiltInRegistries.ITEM.getKey(itemResource.item()).toString());
        return tag;
    }

    public static CompoundTag toTagWithAmount(final ResourceAmount<ItemResource> resourceAmount) {
        final CompoundTag tag = toTag(resourceAmount.getResource());
        tag.putLong(TAG_AMOUNT, resourceAmount.getAmount());
        return tag;
    }

    @SuppressWarnings("deprecation") // forge deprecates Registry access
    public static Optional<ItemResource> fromTag(final CompoundTag tag) {
        final ResourceLocation id = new ResourceLocation(tag.getString(TAG_ID));
        final Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return Optional.empty();
        }
        final CompoundTag itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new ItemResource(item, itemTag));
    }

    public static Optional<ResourceAmount<ItemResource>> fromTagWithAmount(final CompoundTag tag) {
        return fromTag(tag).map(itemResource -> new ResourceAmount<>(itemResource, tag.getLong(TAG_AMOUNT)));
    }
}
