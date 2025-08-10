package com.refinedmods.refinedstorage.common.grid.screen.hint;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage.common.api.support.resource.FluidOperationResult;
import com.refinedmods.refinedstorage.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.common.support.tooltip.MouseClientTooltipComponent;

import java.util.Optional;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class FluidGridInsertionHint implements GridInsertionHint {
    @Override
    public Optional<ClientTooltipComponent> getHint(final ItemStack carried) {
        return Platform.INSTANCE.drainContainer(carried).map(this::createComponent);
    }

    private ClientTooltipComponent createComponent(final FluidOperationResult result) {
        final long amount = Math.min(result.amount(), Platform.INSTANCE.getBucketAmount());
        return MouseClientTooltipComponent.fluid(
            MouseClientTooltipComponent.Type.RIGHT,
            (FluidResource) result.fluid(),
            amount == Platform.INSTANCE.getBucketAmount() ? null : RefinedStorageClientApi.INSTANCE
                .getResourceRendering(FluidResource.class).formatAmount(amount)
        );
    }
}
