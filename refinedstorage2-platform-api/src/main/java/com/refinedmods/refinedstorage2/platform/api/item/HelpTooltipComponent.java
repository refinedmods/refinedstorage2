package com.refinedmods.refinedstorage2.platform.api.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

/**
 * Return this in {@link net.minecraft.world.item.Item#getTooltipImage(ItemStack)} to provide a help tooltip.
 *
 * @param lines the help text lines
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.12")
public record HelpTooltipComponent(List<Component> lines) implements TooltipComponent {
}
