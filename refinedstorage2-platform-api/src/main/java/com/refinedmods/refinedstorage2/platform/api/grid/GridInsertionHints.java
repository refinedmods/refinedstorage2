package com.refinedmods.refinedstorage2.platform.api.grid;

import java.util.List;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.12")
@FunctionalInterface
public interface GridInsertionHints {
    List<ClientTooltipComponent> getHints(ItemStack carried);
}
