package com.refinedmods.refinedstorage.common.api.support.energy;

import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.0.0")
public interface EnergyItemContext {
    EnergyItemContext READONLY = new EnergyItemContext() {
        @Override
        public ItemStack copyStack() {
            return ItemStack.EMPTY;
        }

        @Override
        public void setStack(final ItemStack stack) {
            // no op
        }
    };

    ItemStack copyStack();

    void setStack(ItemStack stack);
}
