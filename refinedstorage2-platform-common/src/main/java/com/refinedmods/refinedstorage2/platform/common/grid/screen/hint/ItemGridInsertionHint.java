package com.refinedmods.refinedstorage2.platform.common.grid.screen.hint;

import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.support.AmountFormatting;
import com.refinedmods.refinedstorage2.platform.common.support.tooltip.MouseWithIconClientTooltipComponent;

import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class ItemGridInsertionHint implements GridInsertionHint {
    @Override
    public Optional<ClientTooltipComponent> getHint(final ItemStack carried) {
        return Optional.of(new MouseWithIconClientTooltipComponent(
            MouseWithIconClientTooltipComponent.Type.LEFT,
            (graphics, x, y) -> graphics.renderItem(carried, x, y),
            carried.getCount() == 1 ? null : AmountFormatting.format(carried.getCount())
        ));
    }
}
