package com.refinedmods.refinedstorage2.platform.api.support.energy;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public interface EnergyItemHelper {
    void addTooltip(ItemStack stack, List<Component> lines);

    boolean isBarVisible(ItemStack stack);

    int getBarWidth(ItemStack stack);

    int getBarColor(ItemStack stack);

    ItemStack createAtEnergyCapacity(Item item);

    void passEnergyToBlockEntity(BlockPos pos, Level level, ItemStack stack);
}
