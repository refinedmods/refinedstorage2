package com.refinedmods.refinedstorage2.platform.api.resource;

import com.refinedmods.refinedstorage2.api.storage.ResourceTemplate;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
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

    <T> void set(int index, ResourceAmountTemplate<T> resourceAmount);

    long getAmount(int index);

    void grow(int index, long amount);

    void shrink(int index, long amount);

    void setAmount(int index, long amount);

    <T> long getMaxAmount(ResourceAmountTemplate<T> resourceAmount);

    void remove(int index);

    int size();

    @Nullable
    ResourceAmountTemplate<?> get(int index);

    Set<Object> getUniqueTemplates();

    List<ResourceTemplate<?>> getTemplates();

    void writeToUpdatePacket(FriendlyByteBuf buf);

    void readFromUpdatePacket(int index, FriendlyByteBuf buf);

    CompoundTag toTag();

    void fromTag(CompoundTag tag);

    List<ClientTooltipComponent> getHelpTooltip(ItemStack carried);

    Container toItemContainer();
}
