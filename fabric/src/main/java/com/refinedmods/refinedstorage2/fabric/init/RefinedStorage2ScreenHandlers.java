package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.handler.DiskDriveScreenHandler;
import com.refinedmods.refinedstorage2.fabric.screen.handler.GridScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class RefinedStorage2ScreenHandlers {
    private ScreenHandlerType<DiskDriveScreenHandler> diskDrive;
    private ScreenHandlerType<GridScreenHandler> grid;

    public void register() {
        diskDrive = ScreenHandlerRegistry.registerSimple(new Identifier(RefinedStorage2Mod.ID, "disk_drive"), DiskDriveScreenHandler::new);
        grid = ScreenHandlerRegistry.registerSimple(new Identifier(RefinedStorage2Mod.ID, "grid"), GridScreenHandler::new);
    }

    public ScreenHandlerType<DiskDriveScreenHandler> getDiskDrive() {
        return diskDrive;
    }

    public ScreenHandlerType<GridScreenHandler> getGrid() {
        return grid;
    }
}
