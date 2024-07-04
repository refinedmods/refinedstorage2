package com.refinedmods.refinedstorage.platform.api.grid.strategy;

import com.refinedmods.refinedstorage.platform.api.grid.Grid;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
@FunctionalInterface
public interface GridExtractionStrategyFactory {
    GridExtractionStrategy create(AbstractContainerMenu containerMenu, ServerPlayer player, Grid grid);
}
