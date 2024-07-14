package com.refinedmods.refinedstorage.platform.fabric.grid.strategy;

import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridAllowExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.view.PlatformGridResource;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResource;
import com.refinedmods.refinedstorage.platform.fabric.util.SimpleSingleStackStorage;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;

import static com.refinedmods.refinedstorage.platform.fabric.support.resource.VariantUtil.toFluidVariant;

public class FluidGridAllowExtractionStrategy implements GridAllowExtractionStrategy {
    @Override
    public boolean allowExtraction(final PlatformGridResource resource, final ItemStack carriedStack) {
        final SimpleSingleStackStorage interceptingStorage = SimpleSingleStackStorage.forStack(carriedStack.copy());
        final net.fabricmc.fabric.api.transfer.v1.storage.Storage<FluidVariant> dest = FluidStorage.ITEM.find(
            interceptingStorage.getStack(),
            ContainerItemContext.ofSingleSlot(interceptingStorage)
        );
        if (dest == null) {
            return true;
        }
        if (resource.getUnderlyingResource() instanceof FluidResource fluidResource) {
            try (Transaction tx = Transaction.openOuter()) {
                final long inserted = dest.insert(toFluidVariant(fluidResource), resource.getAmount(), tx);

                final ContainerItemContext ctx = ContainerItemContext.withConstant(ItemVariant.of(carriedStack),
                    carriedStack.getCount());

                return FluidStorage.ITEM.find(carriedStack, ctx) != null
                    && carriedStack.getCount() == 1
                    && inserted > 0;
            }
        }
        return true;
    }
}
