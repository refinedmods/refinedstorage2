package com.refinedmods.refinedstorage2.platform.api.support.resource;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * Represents a {@link Container} that can hold any resource type.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public interface ResourceContainer {
    ResourceContainerType getType();

    void setListener(@Nullable Runnable listener);

    void change(int index, ItemStack stack, boolean tryAlternatives);

    void set(int index, ResourceAmountTemplate resourceAmount);

    long getAmount(int index);

    void grow(int index, long amount);

    void shrink(int index, long amount);

    void setAmount(int index, long amount);

    long getMaxAmount(ResourceAmountTemplate resourceAmount);

    boolean isValid(ResourceKey resource);

    void remove(int index);

    int size();

    @Nullable
    ResourceAmountTemplate get(int index);

    Set<ResourceKey> getUniqueTemplates();

    List<ResourceTemplate> getTemplates();

    void writeToUpdatePacket(FriendlyByteBuf buf);

    void readFromUpdatePacket(int index, FriendlyByteBuf buf);

    CompoundTag toTag();

    void fromTag(CompoundTag tag);

    ResourceFactory getPrimaryResourceFactory();

    Set<ResourceFactory> getAlternativeResourceFactories();

    Container toItemContainer();

    long insert(StorageChannelType storageChannelType, ResourceKey resource, long amount, Action action);

    long extract(ResourceKey resource, long amount, Action action);

    ResourceContainer copy();
}
