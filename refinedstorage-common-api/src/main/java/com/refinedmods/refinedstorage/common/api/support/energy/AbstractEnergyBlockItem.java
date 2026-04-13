package com.refinedmods.refinedstorage.common.api.support.energy;

import java.util.function.Consumer;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.1")
public abstract class AbstractEnergyBlockItem extends BlockItem {
    private final EnergyItemHelper helper;

    protected AbstractEnergyBlockItem(final Block block, final Properties properties, final EnergyItemHelper helper) {
        super(block, properties);
        this.helper = helper;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(final ItemStack stack,
                                final TooltipContext context,
                                final TooltipDisplay display,
                                final Consumer<Component> builder,
                                final TooltipFlag flag) {
        super.appendHoverText(stack, context, display, builder, flag);
        helper.addTooltip(stack, builder);
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
