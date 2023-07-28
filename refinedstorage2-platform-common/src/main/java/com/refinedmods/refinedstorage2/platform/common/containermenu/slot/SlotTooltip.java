package com.refinedmods.refinedstorage2.platform.common.containermenu.slot;

import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface SlotTooltip {
    List<ClientTooltipComponent> getTooltip(ItemStack carried);
}
