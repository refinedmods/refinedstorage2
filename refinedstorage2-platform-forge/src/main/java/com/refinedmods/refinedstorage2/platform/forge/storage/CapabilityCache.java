package com.refinedmods.refinedstorage2.platform.forge.storage;

import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import javax.annotation.Nullable;

import com.google.common.collect.AbstractIterator;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import static com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource.ofItemStack;
import static com.refinedmods.refinedstorage2.platform.forge.support.resource.VariantUtil.ofFluidStack;

public interface CapabilityCache {
    default Optional<IItemHandler> getItemHandler() {
        return Optional.empty();
    }

    default Iterator<ItemResource> getItemIterator() {
        return getItemHandler().map(handler -> (Iterator<ItemResource>) new AbstractIterator<ItemResource>() {
            private int index;

            @Nullable
            @Override
            protected ItemResource computeNext() {
                if (index > handler.getSlots()) {
                    return endOfData();
                }
                for (; index < handler.getSlots(); ++index) {
                    final ItemStack slot = handler.getStackInSlot(index);
                    if (!slot.isEmpty()) {
                        index++;
                        return ofItemStack(slot);
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }

    default Iterator<ResourceAmount<ItemResource>> getItemAmountIterator() {
        return getItemHandler().map(
            handler -> (Iterator<ResourceAmount<ItemResource>>) new AbstractIterator<ResourceAmount<ItemResource>>() {
                private int index;

                @Nullable
                @Override
                protected ResourceAmount<ItemResource> computeNext() {
                    if (index > handler.getSlots()) {
                        return endOfData();
                    }
                    for (; index < handler.getSlots(); ++index) {
                        final ItemStack slot = handler.getStackInSlot(index);
                        if (!slot.isEmpty()) {
                            index++;
                            return new ResourceAmount<>(ofItemStack(slot), slot.getCount());
                        }
                    }
                    return endOfData();
                }
            }
        ).orElse(Collections.emptyListIterator());
    }

    default Optional<IFluidHandler> getFluidHandler() {
        return Optional.empty();
    }

    default Iterator<FluidResource> getFluidIterator() {
        return getFluidHandler().map(handler -> (Iterator<FluidResource>) new AbstractIterator<FluidResource>() {
            private int index;

            @Nullable
            @Override
            protected FluidResource computeNext() {
                if (index > handler.getTanks()) {
                    return endOfData();
                }
                for (; index < handler.getTanks(); ++index) {
                    final FluidStack slot = handler.getFluidInTank(index);
                    if (!slot.isEmpty()) {
                        index++;
                        return ofFluidStack(slot);
                    }
                }
                return endOfData();
            }
        }).orElse(Collections.emptyListIterator());
    }

    default Iterator<ResourceAmount<FluidResource>> getFluidAmountIterator() {
        return getFluidHandler().map(
            handler -> (Iterator<ResourceAmount<FluidResource>>) new AbstractIterator<ResourceAmount<FluidResource>>() {
                private int index;

                @Nullable
                @Override
                protected ResourceAmount<FluidResource> computeNext() {
                    if (index > handler.getTanks()) {
                        return endOfData();
                    }
                    for (; index < handler.getTanks(); ++index) {
                        final FluidStack slot = handler.getFluidInTank(index);
                        if (!slot.isEmpty()) {
                            index++;
                            return new ResourceAmount<>(ofFluidStack(slot), slot.getAmount());
                        }
                    }
                    return endOfData();
                }
            }
        ).orElse(Collections.emptyListIterator());
    }

    static CapabilityCache ofItemHandler(final IItemHandler itemHandler) {
        return new CapabilityCache() {
            @Override
            public Optional<IItemHandler> getItemHandler() {
                return Optional.of(itemHandler);
            }
        };
    }
}
