package com.refinedmods.refinedstorage2.platform.common.screen.grid.hint;

import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.ResourceTooltipComponent;

import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class SingleItemGridInsertionHint implements GridInsertionHint {
    @Override
    public Optional<ClientTooltipComponent> getHint(final ItemStack carried) {
        return Optional.of(new ResourceTooltipComponent(
            false,
            (graphics, x, y) -> graphics.renderItem(carried, x, y),
            null
        ));
    }
}
