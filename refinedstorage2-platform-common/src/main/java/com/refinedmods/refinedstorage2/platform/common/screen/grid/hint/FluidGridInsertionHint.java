package com.refinedmods.refinedstorage2.platform.common.screen.grid.hint;

import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.ContainedFluid;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.screen.tooltip.MouseWithIconClientTooltipComponent;

import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class FluidGridInsertionHint implements GridInsertionHint {
    @Override
    public Optional<ClientTooltipComponent> getHint(final ItemStack carried) {
        return Platform.INSTANCE.getContainedFluid(carried).map(this::createComponent);
    }

    private MouseWithIconClientTooltipComponent createComponent(final ContainedFluid result) {
        return new MouseWithIconClientTooltipComponent(
            MouseWithIconClientTooltipComponent.Type.RIGHT,
            (graphics, x, y) -> Platform.INSTANCE.getFluidRenderer().render(
                graphics.pose(),
                x,
                y,
                result.fluid().getResource()
            ),
            result.fluid().getAmount() == Platform.INSTANCE.getBucketAmount()
                ? null
                : Platform.INSTANCE.getBucketAmountFormatter().format(result.fluid().getAmount())
        );
    }
}
