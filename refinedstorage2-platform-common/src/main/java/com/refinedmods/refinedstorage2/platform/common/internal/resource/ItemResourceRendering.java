package com.refinedmods.refinedstorage2.platform.common.internal.resource;

import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.api.util.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.util.ClientProxy;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemResourceRendering implements ResourceRendering<ItemResource> {
    private final Map<ItemResource, ItemStack> stackCache = new HashMap<>();

    private ItemStack getStack(final ItemResource itemResource) {
        return stackCache.computeIfAbsent(itemResource, ItemResource::toItemStack);
    }

    @Override
    public String getDisplayedAmount(final long amount) {
        if (amount == 1) {
            return "";
        }
        return AmountFormatting.formatWithUnits(amount);
    }

    @Override
    public Component getDisplayName(final ItemResource resource) {
        return getStack(resource).getHoverName();
    }

    @Override
    public List<Component> getTooltip(final ItemResource resource) {
        final Minecraft minecraft = Minecraft.getInstance();
        return ClientProxy.getPlayer().map(player -> getStack(resource).getTooltipLines(
            player,
            minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        )).orElse(Collections.emptyList());
    }

    @Override
    public void render(final ItemResource resource, final GuiGraphics graphics, final int x, final int y) {
        final ItemStack stack = getStack(resource);
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
    }
}
