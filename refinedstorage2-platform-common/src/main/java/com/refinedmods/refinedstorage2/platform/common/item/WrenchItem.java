package com.refinedmods.refinedstorage2.platform.common.item;

import com.refinedmods.refinedstorage2.platform.api.item.HelpTooltipComponent;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

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
