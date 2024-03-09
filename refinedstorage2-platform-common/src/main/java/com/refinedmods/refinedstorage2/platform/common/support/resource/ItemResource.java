package com.refinedmods.refinedstorage2.platform.common.support.resource;

import com.refinedmods.refinedstorage2.api.core.CoreValidations;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FuzzyModeNormalizer;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record ItemResource(Item item, @Nullable CompoundTag tag) implements PlatformResourceKey, FuzzyModeNormalizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemResource.class);

    private static final String TAG_TAG = "tag";
    private static final String TAG_ID = "id";

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

    @Override
    public ResourceKey normalize() {
        return new ItemResource(item, null);
    }

    @Override
    public CompoundTag toTag() {
        final CompoundTag nbt = new CompoundTag();
        if (this.tag != null) {
            nbt.put(TAG_TAG, this.tag);
        }
        nbt.putString(TAG_ID, BuiltInRegistries.ITEM.getKey(item).toString());
        return nbt;
    }

    @Override
    public void toBuffer(final FriendlyByteBuf buf) {
        buf.writeVarInt(Item.getId(item));
        buf.writeNbt(tag);
    }

    @Override
    public long getInterfaceExportLimit() {
        return item.getMaxStackSize();
    }

    @Override
    public ResourceType getResourceType() {
        return ResourceTypes.ITEM;
    }

    public static ItemResource ofItemStack(final ItemStack itemStack) {
        return new ItemResource(itemStack.getItem(), itemStack.getTag());
    }

    static Optional<PlatformResourceKey> fromTag(final CompoundTag tag) {
        final ResourceLocation id = new ResourceLocation(tag.getString(TAG_ID));
        final Item item = BuiltInRegistries.ITEM.get(id);
        if (item == Items.AIR) {
            return Optional.empty();
        }
        final CompoundTag itemTag = tag.contains(TAG_TAG) ? tag.getCompound(TAG_TAG) : null;
        return Optional.of(new ItemResource(item, itemTag));
    }
}
