package com.refinedmods.refinedstorage.common.autocrafting.monitor;

import com.refinedmods.refinedstorage.common.content.Menus;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.containermenu.ServerProperty;

import java.util.function.Predicate;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class AutocraftingMonitorContainerMenu extends AbstractAutocraftingMonitorContainerMenu {
    private final Predicate<Player> stillValid;

    public AutocraftingMonitorContainerMenu(final int syncId,
                                            final Inventory playerInventory,
                                            final AutocraftingMonitorData data) {
        super(Menus.INSTANCE.getAutocraftingMonitor(), syncId, playerInventory, data);
        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));
        this.stillValid = p -> true;
    }

    AutocraftingMonitorContainerMenu(final int syncId,
                                     final Player player,
                                     final AutocraftingMonitorBlockEntity autocraftingMonitor) {
        super(Menus.INSTANCE.getAutocraftingMonitor(), syncId, player, autocraftingMonitor);
        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            autocraftingMonitor::getRedstoneMode,
            autocraftingMonitor::setRedstoneMode
        ));
        this.stillValid = p -> Container.stillValidBlockEntity(autocraftingMonitor, p);
    }

    @Override
    public boolean stillValid(final Player player) {
        return stillValid.test(player);
    }
}
