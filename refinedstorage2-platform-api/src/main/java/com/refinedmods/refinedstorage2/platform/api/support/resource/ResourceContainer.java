package com.refinedmods.refinedstorage2.platform.api.support.resource;

import com.refinedmods.refinedstorage2.api.core.Action;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * Represents a {@link Container} that can hold any {@link ResourceType}.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public interface ResourceContainer {
    void setListener(@Nullable Runnable listener);

    void change(int index, ItemStack stack, boolean tryAlternatives);

    void set(int index, ResourceAmount resourceAmount);

    long getAmount(int index);

    void grow(int index, long amount);

    void shrink(int index, long amount);

    void setAmount(int index, long amount);

    long getMaxAmount(ResourceKey resource);

    boolean isValid(ResourceKey resource);

    void remove(int index);

    int size();

    default boolean isEmpty(int index) {
        return get(index) == null;
    }

    @Nullable
    ResourceAmount get(int index);

    @Nullable
    PlatformResourceKey getResource(int index);

    ItemStack getStackRepresentation(int index);

    Set<ResourceKey> getUniqueResources();

    List<ResourceKey> getResources();

    void writeToUpdatePacket(FriendlyByteBuf buf);

    void readFromUpdatePacket(int index, FriendlyByteBuf buf);

    CompoundTag toTag();

    void fromTag(CompoundTag tag);

    ResourceFactory getPrimaryResourceFactory();

    Set<ResourceFactory> getAlternativeResourceFactories();

    Container toItemContainer();

    long insert(ResourceKey resource, long amount, Action action);

    long extract(ResourceKey resource, long amount, Action action);

    ResourceContainer copy();
}
