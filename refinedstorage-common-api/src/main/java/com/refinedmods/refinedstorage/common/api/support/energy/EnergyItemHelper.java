package com.refinedmods.refinedstorage.common.api.support.energy;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface EnergyItemHelper {
    void addTooltip(ItemStack stack, Consumer<Component> builder);

    boolean isBarVisible(ItemStack stack);

    int getBarWidth(ItemStack stack);

    int getBarColor(ItemStack stack);

    ItemStack createAtEnergyCapacity(Item item);
}
