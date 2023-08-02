package com.refinedmods.refinedstorage2.platform.common.item.block;

import com.refinedmods.refinedstorage2.platform.api.item.HelpTooltipComponent;

import java.util.Optional;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class NamedBlockItem extends BlockItem {
    private final Component name;
    @Nullable
    private final Component helpText;

    public NamedBlockItem(final Block block,
                          final Properties properties,
                          final Component name) {
        this(block, properties, name, null);
    }

    public NamedBlockItem(final Block block,
                          final Properties properties,
                          final Component name,
                          @Nullable final Component helpText) {
        super(block, properties);
        this.name = name;
        this.helpText = helpText;
    }

    @Override
    public Component getDescription() {
        return name;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return name;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        if (helpText == null) {
            return Optional.empty();
        }
        return Optional.of(new HelpTooltipComponent(helpText));
    }
}
