package com.refinedmods.refinedstorage.common.misc;

import com.refinedmods.refinedstorage.common.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage.common.content.ContentIds;

import java.util.Optional;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class WrenchItem extends Item {
    private static final Component HELP = createTranslation("item", "wrench.help");

    public WrenchItem() {
        super(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, ContentIds.WRENCH)));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }
}
