package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ConstructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.DestructorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ExporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ImporterContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.RegulatorUpgradeContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.detector.DetectorContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.CraftingGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ExternalStorageContainerMenu;
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
    private Supplier<MenuType<GridContainerMenu>> grid;
    @Nullable
    private Supplier<MenuType<CraftingGridContainerMenu>> craftingGrid;
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
    @Nullable
    private Supplier<MenuType<ExternalStorageContainerMenu>> externalStorage;
    @Nullable
    private Supplier<MenuType<DetectorContainerMenu>> detector;
    @Nullable
    private Supplier<MenuType<DestructorContainerMenu>> destructor;
    @Nullable
    private Supplier<MenuType<ConstructorContainerMenu>> constructor;
    @Nullable
    private Supplier<MenuType<RegulatorUpgradeContainerMenu>> regulatorUpgrade;

    private Menus() {
    }

    public MenuType<DiskDriveContainerMenu> getDiskDrive() {
        return Objects.requireNonNull(diskDrive).get();
    }

    public MenuType<GridContainerMenu> getGrid() {
        return Objects.requireNonNull(grid).get();
    }

    public MenuType<CraftingGridContainerMenu> getCraftingGrid() {
        return Objects.requireNonNull(craftingGrid).get();
    }

    public void setCraftingGrid(final Supplier<MenuType<CraftingGridContainerMenu>> supplier) {
        this.craftingGrid = supplier;
    }

    public MenuType<ControllerContainerMenu> getController() {
        return Objects.requireNonNull(controller).get();
    }

    public void setDiskDrive(final Supplier<MenuType<DiskDriveContainerMenu>> supplier) {
        this.diskDrive = supplier;
    }

    public void setGrid(final Supplier<MenuType<GridContainerMenu>> supplier) {
        this.grid = supplier;
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

    public MenuType<ExternalStorageContainerMenu> getExternalStorage() {
        return Objects.requireNonNull(externalStorage).get();
    }

    public void setExternalStorage(final Supplier<MenuType<ExternalStorageContainerMenu>> supplier) {
        this.externalStorage = supplier;
    }

    public MenuType<DetectorContainerMenu> getDetector() {
        return Objects.requireNonNull(detector).get();
    }

    public void setDetector(final Supplier<MenuType<DetectorContainerMenu>> supplier) {
        this.detector = supplier;
    }

    public MenuType<DestructorContainerMenu> getDestructor() {
        return Objects.requireNonNull(destructor).get();
    }

    public void setDestructor(final Supplier<MenuType<DestructorContainerMenu>> supplier) {
        this.destructor = supplier;
    }

    public MenuType<ConstructorContainerMenu> getConstructor() {
        return Objects.requireNonNull(constructor).get();
    }

    public void setConstructor(final Supplier<MenuType<ConstructorContainerMenu>> supplier) {
        this.constructor = supplier;
    }

    public MenuType<RegulatorUpgradeContainerMenu> getRegulatorUpgrade() {
        return Objects.requireNonNull(regulatorUpgrade).get();
    }

    public void setRegulatorUpgrade(final Supplier<MenuType<RegulatorUpgradeContainerMenu>> supplier) {
        this.regulatorUpgrade = supplier;
    }
}
