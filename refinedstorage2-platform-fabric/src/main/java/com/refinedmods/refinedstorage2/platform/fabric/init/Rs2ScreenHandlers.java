package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.ControllerScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.diskdrive.DiskDriveScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.FluidGridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.ItemGridScreenHandler;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;

public class Rs2ScreenHandlers {
    private ScreenHandlerType<DiskDriveScreenHandler> diskDrive;
    private ScreenHandlerType<ItemGridScreenHandler> grid;
    private ScreenHandlerType<FluidGridScreenHandler> fluidGrid;
    private ScreenHandlerType<ControllerScreenHandler> controller;

    public void register() {
        diskDrive = ScreenHandlerRegistry.registerSimple(Rs2Mod.createIdentifier("disk_drive"), DiskDriveScreenHandler::new);
        grid = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("grid"), ItemGridScreenHandler::new);
        fluidGrid = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("fluid_grid"), FluidGridScreenHandler::new);
        controller = ScreenHandlerRegistry.registerExtended(Rs2Mod.createIdentifier("controller"), ControllerScreenHandler::new);
    }

    public ScreenHandlerType<DiskDriveScreenHandler> getDiskDrive() {
        return diskDrive;
    }

    public ScreenHandlerType<ItemGridScreenHandler> getGrid() {
        return grid;
    }

    public ScreenHandlerType<FluidGridScreenHandler> getFluidGrid() {
        return fluidGrid;
    }

    public ScreenHandlerType<ControllerScreenHandler> getController() {
        return controller;
    }
}
