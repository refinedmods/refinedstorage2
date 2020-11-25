package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.handler.DiskDriveScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;

public class RefinedStorage2ScreenHandlers {
    private ScreenHandlerType<DiskDriveScreenHandler> diskDrive;

    public void register() {
        diskDrive = ScreenHandlerRegistry.registerSimple(new Identifier(RefinedStorage2Mod.ID, "disk_drive"), DiskDriveScreenHandler::new);
    }

    public ScreenHandlerType<DiskDriveScreenHandler> getDiskDrive() {
        return diskDrive;
    }
}
