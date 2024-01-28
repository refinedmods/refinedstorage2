package com.refinedmods.refinedstorage2.platform.common.controller;

import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.energy.EnergyStorageImpl;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.HelpTooltipComponent;
import com.refinedmods.refinedstorage2.platform.api.support.energy.AbstractEnergyBlockItem;
import com.refinedmods.refinedstorage2.platform.common.Platform;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ControllerBlockItem extends AbstractEnergyBlockItem {
    private final Component name;

    ControllerBlockItem(final Block block, final Component displayName) {
        super(block, new Item.Properties().stacksTo(1), PlatformApi.INSTANCE.getEnergyItemHelper());
        this.name = displayName;
    }

    @Override
    public Component getDescription() {
        return name;
    }

    @Override
    public Component getName(final ItemStack stack) {
        return name;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(final ItemStack stack) {
        return Optional.of(new HelpTooltipComponent(createTranslation("item", "controller.help")));
    }

    public EnergyStorage createEnergyStorage(final ItemStack stack) {
        final EnergyStorage energyStorage = new EnergyStorageImpl(
            Platform.INSTANCE.getConfig().getController().getEnergyCapacity()
        );
        return PlatformApi.INSTANCE.asItemEnergyStorage(energyStorage, stack);
    }
}
