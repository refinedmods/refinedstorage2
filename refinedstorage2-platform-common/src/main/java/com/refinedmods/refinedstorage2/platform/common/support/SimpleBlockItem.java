package com.refinedmods.refinedstorage2.platform.common.support;

import com.refinedmods.refinedstorage2.platform.api.support.HelpTooltipComponent;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class SimpleBlockItem extends BlockItem {
    @Nullable
    private final Component helpText;

    public SimpleBlockItem(final Block block) {
        this(block, null);
    }

    public SimpleBlockItem(final Block block, @Nullable final Component helpText) {
        super(block, new Item.Properties());
        this.helpText = helpText;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        if (helpText == null) {
            return Optional.empty();
        }
        return Optional.of(new HelpTooltipComponent(helpText));
    }
}
