package com.refinedmods.refinedstorage2.platform.api.support.energy;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public abstract class AbstractEnergyItem extends Item implements EnergyItem {
    private final EnergyItemHelper helper;

    protected AbstractEnergyItem(final Properties properties, final EnergyItemHelper helper) {
        super(properties);
        this.helper = helper;
    }

    @Override
    public void appendHoverText(
        final ItemStack stack,
        @Nullable final Level level,
        final List<Component> lines,
        final TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, lines, flag);
        helper.addTooltip(stack, lines);
    }

    @Override
    public boolean isBarVisible(final ItemStack stack) {
        return helper.isBarVisible(stack);
    }

    @Override
    public int getBarWidth(final ItemStack stack) {
        return helper.getBarWidth(stack);
    }

    @Override
    public int getBarColor(final ItemStack stack) {
        return helper.getBarColor(stack);
    }

    public ItemStack createAtEnergyCapacity() {
        return helper.createAtEnergyCapacity(this);
    }
}
