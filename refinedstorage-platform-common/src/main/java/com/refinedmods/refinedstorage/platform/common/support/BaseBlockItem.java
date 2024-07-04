package com.refinedmods.refinedstorage.platform.common.support;

import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class BaseBlockItem extends BlockItem {
    private final Block block;
    @Nullable
    private final Component helpText;

    public BaseBlockItem(final Block block) {
        this(block, null);
    }

    public BaseBlockItem(final Block block, @Nullable final Component helpText) {
        this(block, new Properties(), helpText);
    }

    public BaseBlockItem(final Block block, final Properties properties, @Nullable final Component helpText) {
        super(block, properties);
        this.block = block;
        this.helpText = helpText;
    }

    @Override
    public Component getDescription() {
        return block.getName();
    }

    @Override
    public Component getName(final ItemStack stack) {
        return block.getName();
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        if (helpText == null) {
            return Optional.empty();
        }
        return Optional.of(new HelpTooltipComponent(helpText));
    }
}
