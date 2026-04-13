package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;

import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public class BaseBlockItem extends BlockItem {
    private final Block block;
    @Nullable
    private final Component helpText;

    public BaseBlockItem(final Identifier id, final Block block) {
        this(id, block, null);
    }

    public BaseBlockItem(final Identifier id, final Block block, @Nullable final Component helpText) {
        this(block, new Properties().useBlockDescriptionPrefix().overrideDescription(block.getDescriptionId()).setId(
            ResourceKey.create(Registries.ITEM, id)), helpText);
    }

    public BaseBlockItem(final Block block, final Properties properties, @Nullable final Component helpText) {
        super(block, properties);
        this.block = block;
        this.helpText = helpText;
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
