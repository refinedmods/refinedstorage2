package com.refinedmods.refinedstorage2.platform.api.support;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * Return this in {@link net.minecraft.world.item.Item#getTooltipImage(ItemStack)} to provide a help tooltip.
 *
 * @param text the help text
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.12")
public record HelpTooltipComponent(Component text) implements TooltipComponent {
}
