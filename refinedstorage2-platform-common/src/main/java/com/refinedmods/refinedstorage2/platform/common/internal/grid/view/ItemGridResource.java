package com.refinedmods.refinedstorage2.platform.common.internal.grid.view;

import com.refinedmods.refinedstorage2.api.core.QuantityFormatter;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemGridResource extends AbstractGridResource<ItemResource> {
    private final int id;
    private final ItemStack itemStack;

    public ItemGridResource(final ResourceAmount<ItemResource> resourceAmount,
                            final ItemStack itemStack,
                            final String name,
                            final String modId,
                            final String modName,
                            final Set<String> tags,
                            final String tooltip) {
        super(resourceAmount, name, Map.of(
            GridResourceAttributeKeys.MOD_ID, Set.of(modId),
            GridResourceAttributeKeys.MOD_NAME, Set.of(modName),
            GridResourceAttributeKeys.TAGS, tags,
            GridResourceAttributeKeys.TOOLTIP, Set.of(tooltip)
        ));
        this.id = Item.getId(resourceAmount.getResource().item());
        this.itemStack = itemStack;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void onExtract(final GridExtractMode extractMode,
                          final boolean cursor,
                          final GridExtractionStrategy extractionStrategy) {
        extractionStrategy.onExtract(
            StorageChannelTypes.ITEM,
            resourceAmount.getResource(),
            extractMode,
            cursor
        );
    }

    @Override
    public void onScroll(final GridScrollMode scrollMode, final GridScrollingStrategy scrollingStrategy) {
        scrollingStrategy.onScroll(
            StorageChannelTypes.ITEM,
            resourceAmount.getResource(),
            scrollMode,
            -1
        );
    }

    @Override
    public void render(final PoseStack poseStack, final int slotX, final int slotY) {
        final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        final Font font = Minecraft.getInstance().font;
        itemRenderer.renderGuiItem(itemStack, slotX, slotY);
        itemRenderer.renderGuiItemDecorations(font, itemStack, slotX, slotY, null);
    }

    @Override
    public String getDisplayedAmount() {
        return QuantityFormatter.formatWithUnits(getAmount());
    }

    @Override
    public String getAmountInTooltip() {
        return QuantityFormatter.format(getAmount());
    }

    @Override
    public List<Component> getTooltip() {
        final Minecraft minecraft = Minecraft.getInstance();
        return itemStack.getTooltipLines(
            minecraft.player,
            minecraft.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL
        );
    }
}
