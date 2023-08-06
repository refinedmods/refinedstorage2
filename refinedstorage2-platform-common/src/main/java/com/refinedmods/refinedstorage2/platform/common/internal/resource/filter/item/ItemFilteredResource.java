package com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResource;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.util.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.util.ClientProxy;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public record ItemFilteredResource(ItemResource value, long amount, ItemStack stack)
    implements FilteredResource<ItemResource> {
    public ItemFilteredResource(final ItemResource value, final long amount) {
        this(value, amount, value.toItemStack(amount));
    }

    @Override
    public void render(final GuiGraphics graphics, final int x, final int y) {
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
    }

    @Override
    public ItemResource getValue() {
        return value;
    }

    @Override
    public FilteredResource<ItemResource> withAmount(final long newAmount) {
        return new ItemFilteredResource(value, newAmount);
    }

    @Override
    public long getAmount() {
        return amount;
    }

    @Override
    public long getMaxAmount() {
        return stack.getMaxStackSize();
    }

    @Override
    public String getDisplayedAmount() {
        if (amount == 1) {
            return "";
        }
        return AmountFormatting.formatWithUnits(amount);
    }

    @Override
    public Component getDisplayName() {
        return stack.getHoverName();
    }

    @Override
    public List<Component> getTooltip() {
        final Minecraft minecraft = Minecraft.getInstance();
        return ClientProxy.getPlayer().map(player -> stack.getTooltipLines(
            player,
            minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        )).orElse(Collections.emptyList());
    }

    @Override
    public PlatformStorageChannelType<ItemResource> getStorageChannelType() {
        return StorageChannelTypes.ITEM;
    }
}
