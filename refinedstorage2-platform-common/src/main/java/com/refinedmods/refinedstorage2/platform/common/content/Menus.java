package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;

import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.inventory.MenuType;

public final class Menus {
    public static final Menus INSTANCE = new Menus();

    @Nullable
    private Supplier<MenuType<DiskDriveContainerMenu>> diskDrive;
    @Nullable
    private Supplier<MenuType<ItemGridContainerMenu>> grid;
    @Nullable
    private Supplier<MenuType<FluidGridContainerMenu>> fluidGrid;
    @Nullable
    private Supplier<MenuType<ControllerContainerMenu>> controller;
    @Nullable
    private Supplier<MenuType<ItemStorageBlockContainerMenu>> itemStorage;
    @Nullable
    private Supplier<MenuType<FluidStorageBlockContainerMenu>> fluidStorage;
    @Nullable
    private Supplier<MenuType<ImporterContainerMenu>> importer;
    @Nullable
    private Supplier<MenuType<ExporterContainerMenu>> exporter;
    @Nullable
    private Supplier<MenuType<InterfaceContainerMenu>> iface;

    private Menus() {
    }

    public MenuType<DiskDriveContainerMenu> getDiskDrive() {
        return Objects.requireNonNull(diskDrive).get();
    }

    public MenuType<ItemGridContainerMenu> getGrid() {
        return Objects.requireNonNull(grid).get();
    }

    public MenuType<FluidGridContainerMenu> getFluidGrid() {
        return Objects.requireNonNull(fluidGrid).get();
    }

    public MenuType<ControllerContainerMenu> getController() {
        return Objects.requireNonNull(controller).get();
    }

    public void setDiskDrive(final Supplier<MenuType<DiskDriveContainerMenu>> supplier) {
        this.diskDrive = supplier;
    }

    public void setGrid(final Supplier<MenuType<ItemGridContainerMenu>> supplier) {
        this.grid = supplier;
    }

    public void setFluidGrid(final Supplier<MenuType<FluidGridContainerMenu>> supplier) {
        this.fluidGrid = supplier;
    }

    public void setController(final Supplier<MenuType<ControllerContainerMenu>> supplier) {
        this.controller = supplier;
    }

    public MenuType<ItemStorageBlockContainerMenu> getItemStorage() {
        return Objects.requireNonNull(itemStorage).get();
    }

    public void setItemStorage(final Supplier<MenuType<ItemStorageBlockContainerMenu>> supplier) {
        this.itemStorage = supplier;
    }

    public MenuType<FluidStorageBlockContainerMenu> getFluidStorage() {
        return Objects.requireNonNull(fluidStorage).get();
    }

    public void setFluidStorage(final Supplier<MenuType<FluidStorageBlockContainerMenu>> supplier) {
        this.fluidStorage = supplier;
    }

    public MenuType<ImporterContainerMenu> getImporter() {
        return Objects.requireNonNull(importer).get();
    }

    public void setImporter(final Supplier<MenuType<ImporterContainerMenu>> supplier) {
        this.importer = supplier;
    }

    public MenuType<ExporterContainerMenu> getExporter() {
        return Objects.requireNonNull(exporter).get();
    }

    public void setExporter(final Supplier<MenuType<ExporterContainerMenu>> supplier) {
        this.exporter = supplier;
    }

    public MenuType<InterfaceContainerMenu> getInterface() {
        return Objects.requireNonNull(iface).get();
    }

    public void setInterface(final Supplier<MenuType<InterfaceContainerMenu>> supplier) {
        this.iface = supplier;
    }
}
