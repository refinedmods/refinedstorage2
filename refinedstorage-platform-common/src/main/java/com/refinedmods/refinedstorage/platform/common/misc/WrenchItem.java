package com.refinedmods.refinedstorage.platform.common.misc;

import com.refinedmods.refinedstorage.platform.api.support.HelpTooltipComponent;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class WrenchItem extends Item {
    private static final Component HELP = createTranslation("item", "wrench.help");

    public WrenchItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(HELP));
    }
}
