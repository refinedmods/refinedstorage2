package com.refinedmods.refinedstorage.common.content;

import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.autocrafter.AutocrafterBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager.AutocrafterManagerBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.monitor.AutocraftingMonitorBlockEntity;
import com.refinedmods.refinedstorage.common.autocrafting.patterngrid.PatternGridBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.AbstractConstructorBlockEntity;
import com.refinedmods.refinedstorage.common.constructordestructor.AbstractDestructorBlockEntity;
import com.refinedmods.refinedstorage.common.controller.ControllerBlockEntity;
import com.refinedmods.refinedstorage.common.detector.DetectorBlockEntity;
import com.refinedmods.refinedstorage.common.exporter.AbstractExporterBlockEntity;
import com.refinedmods.refinedstorage.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage.common.grid.GridBlockEntity;
import com.refinedmods.refinedstorage.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.importer.AbstractImporterBlockEntity;
import com.refinedmods.refinedstorage.common.networking.AbstractCableBlockEntity;
import com.refinedmods.refinedstorage.common.networking.NetworkReceiverBlockEntity;
import com.refinedmods.refinedstorage.common.networking.NetworkTransmitterBlockEntity;
import com.refinedmods.refinedstorage.common.networking.RelayBlockEntity;
import com.refinedmods.refinedstorage.common.networking.WirelessTransmitterBlockEntity;
import com.refinedmods.refinedstorage.common.security.SecurityManagerBlockEntity;
import com.refinedmods.refinedstorage.common.storage.FluidStorageVariant;
import com.refinedmods.refinedstorage.common.storage.ItemStorageVariant;
import com.refinedmods.refinedstorage.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage.common.storage.diskinterface.AbstractDiskInterfaceBlockEntity;
import com.refinedmods.refinedstorage.common.storage.externalstorage.AbstractExternalStorageBlockEntity;
import com.refinedmods.refinedstorage.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage.common.storagemonitor.StorageMonitorBlockEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    @Nullable
    private Supplier<BlockEntityType<AbstractCableBlockEntity>> cable;
    @Nullable
    private Supplier<BlockEntityType<AbstractDiskDriveBlockEntity>> diskDrive;
    @Nullable
    private Supplier<BlockEntityType<GridBlockEntity>> grid;
    @Nullable
    private Supplier<BlockEntityType<CraftingGridBlockEntity>> craftingGrid;
    @Nullable
    private Supplier<BlockEntityType<PatternGridBlockEntity>> patternGrid;
    @Nullable
    private Supplier<BlockEntityType<ControllerBlockEntity>> controller;
    @Nullable
    private Supplier<BlockEntityType<ControllerBlockEntity>> creativeController;
    private final Map<ItemStorageVariant, Supplier<BlockEntityType<AbstractNetworkNodeContainerBlockEntity<?>>>>
        itemStorageBlocks = new EnumMap<>(ItemStorageVariant.class);
    private final Map<FluidStorageVariant, Supplier<BlockEntityType<AbstractNetworkNodeContainerBlockEntity<?>>>>
        fluidStorageBlocks = new EnumMap<>(FluidStorageVariant.class);
    @Nullable
    private Supplier<BlockEntityType<AbstractImporterBlockEntity>> importer;
    @Nullable
    private Supplier<BlockEntityType<AbstractExporterBlockEntity>> exporter;
    @Nullable
    private Supplier<BlockEntityType<InterfaceBlockEntity>> iface;
    @Nullable
    private Supplier<BlockEntityType<AbstractExternalStorageBlockEntity>> externalStorage;
    @Nullable
    private Supplier<BlockEntityType<DetectorBlockEntity>> detector;
    @Nullable
    private Supplier<BlockEntityType<AbstractDestructorBlockEntity>> destructor;
    @Nullable
    private Supplier<BlockEntityType<AbstractConstructorBlockEntity>> constructor;
    @Nullable
    private Supplier<BlockEntityType<WirelessTransmitterBlockEntity>> wirelessTransmitter;
    @Nullable
    private Supplier<BlockEntityType<StorageMonitorBlockEntity>> storageMonitor;
    @Nullable
    private Supplier<BlockEntityType<NetworkReceiverBlockEntity>> networkReceiver;
    @Nullable
    private Supplier<BlockEntityType<NetworkTransmitterBlockEntity>> networkTransmitter;
    @Nullable
    private Supplier<BlockEntityType<AbstractPortableGridBlockEntity>> portableGrid;
    @Nullable
    private Supplier<BlockEntityType<AbstractPortableGridBlockEntity>> creativePortableGrid;
    @Nullable
    private Supplier<BlockEntityType<SecurityManagerBlockEntity>> securityManager;
    @Nullable
    private Supplier<BlockEntityType<RelayBlockEntity>> relay;
    @Nullable
    private Supplier<BlockEntityType<AbstractDiskInterfaceBlockEntity>> diskInterface;
    @Nullable
    private Supplier<BlockEntityType<AutocrafterBlockEntity>> autocrafter;
    @Nullable
    private Supplier<BlockEntityType<AutocrafterManagerBlockEntity>> autocrafterManager;
    @Nullable
    private Supplier<BlockEntityType<AutocraftingMonitorBlockEntity>> autocraftingMonitor;

    private BlockEntities() {
    }

    public BlockEntityType<AbstractCableBlockEntity> getCable() {
        return requireNonNull(cable).get();
    }

    public void setCable(final Supplier<BlockEntityType<AbstractCableBlockEntity>> supplier) {
        this.cable = supplier;
    }

    public BlockEntityType<AbstractDiskDriveBlockEntity> getDiskDrive() {
        return requireNonNull(diskDrive).get();
    }

    public void setDiskDrive(final Supplier<BlockEntityType<AbstractDiskDriveBlockEntity>> supplier) {
        this.diskDrive = supplier;
    }

    public BlockEntityType<GridBlockEntity> getGrid() {
        return requireNonNull(grid).get();
    }

    public void setGrid(final Supplier<BlockEntityType<GridBlockEntity>> supplier) {
        this.grid = supplier;
    }

    public BlockEntityType<CraftingGridBlockEntity> getCraftingGrid() {
        return requireNonNull(craftingGrid).get();
    }

    public void setCraftingGrid(final Supplier<BlockEntityType<CraftingGridBlockEntity>> supplier) {
        this.craftingGrid = supplier;
    }

    public BlockEntityType<PatternGridBlockEntity> getPatternGrid() {
        return requireNonNull(patternGrid).get();
    }

    public void setPatternGrid(final Supplier<BlockEntityType<PatternGridBlockEntity>> supplier) {
        this.patternGrid = supplier;
    }

    public BlockEntityType<ControllerBlockEntity> getController() {
        return requireNonNull(controller).get();
    }

    public void setController(final Supplier<BlockEntityType<ControllerBlockEntity>> supplier) {
        this.controller = supplier;
    }

    public BlockEntityType<ControllerBlockEntity> getCreativeController() {
        return requireNonNull(creativeController).get();
    }

    public void setCreativeController(final Supplier<BlockEntityType<ControllerBlockEntity>> supplier) {
        this.creativeController = supplier;
    }

    public void setItemStorageBlock(
        final ItemStorageVariant variant,
        final Supplier<BlockEntityType<AbstractNetworkNodeContainerBlockEntity<?>>> supplier
    ) {
        itemStorageBlocks.put(variant, supplier);
    }

    public BlockEntityType<AbstractNetworkNodeContainerBlockEntity<?>> getItemStorageBlock(
        final ItemStorageVariant variant
    ) {
        return itemStorageBlocks.get(variant).get();
    }

    public void setFluidStorageBlock(
        final FluidStorageVariant variant,
        final Supplier<BlockEntityType<AbstractNetworkNodeContainerBlockEntity<?>>> supplier
    ) {
        fluidStorageBlocks.put(variant, supplier);
    }

    public BlockEntityType<AbstractNetworkNodeContainerBlockEntity<?>> getFluidStorageBlock(
        final FluidStorageVariant variant) {
        return fluidStorageBlocks.get(variant).get();
    }

    public BlockEntityType<AbstractImporterBlockEntity> getImporter() {
        return requireNonNull(importer).get();
    }

    public void setImporter(final Supplier<BlockEntityType<AbstractImporterBlockEntity>> supplier) {
        this.importer = supplier;
    }

    public BlockEntityType<AbstractExporterBlockEntity> getExporter() {
        return requireNonNull(exporter).get();
    }

    public void setExporter(final Supplier<BlockEntityType<AbstractExporterBlockEntity>> supplier) {
        this.exporter = supplier;
    }

    public BlockEntityType<InterfaceBlockEntity> getInterface() {
        return requireNonNull(iface).get();
    }

    public void setInterface(final Supplier<BlockEntityType<InterfaceBlockEntity>> supplier) {
        this.iface = supplier;
    }

    public BlockEntityType<AbstractExternalStorageBlockEntity> getExternalStorage() {
        return requireNonNull(externalStorage).get();
    }

    public void setExternalStorage(final Supplier<BlockEntityType<AbstractExternalStorageBlockEntity>> supplier) {
        this.externalStorage = supplier;
    }

    public BlockEntityType<DetectorBlockEntity> getDetector() {
        return requireNonNull(detector).get();
    }

    public void setDetector(final Supplier<BlockEntityType<DetectorBlockEntity>> supplier) {
        this.detector = supplier;
    }

    public BlockEntityType<AbstractDestructorBlockEntity> getDestructor() {
        return requireNonNull(destructor).get();
    }

    public void setDestructor(final Supplier<BlockEntityType<AbstractDestructorBlockEntity>> supplier) {
        this.destructor = supplier;
    }

    public BlockEntityType<AbstractConstructorBlockEntity> getConstructor() {
        return requireNonNull(constructor).get();
    }

    public void setConstructor(final Supplier<BlockEntityType<AbstractConstructorBlockEntity>> supplier) {
        this.constructor = supplier;
    }

    public BlockEntityType<WirelessTransmitterBlockEntity> getWirelessTransmitter() {
        return requireNonNull(wirelessTransmitter).get();
    }

    public void setWirelessTransmitter(final Supplier<BlockEntityType<WirelessTransmitterBlockEntity>> supplier) {
        this.wirelessTransmitter = supplier;
    }

    public BlockEntityType<StorageMonitorBlockEntity> getStorageMonitor() {
        return requireNonNull(storageMonitor).get();
    }

    public void setStorageMonitor(final Supplier<BlockEntityType<StorageMonitorBlockEntity>> supplier) {
        this.storageMonitor = supplier;
    }

    public BlockEntityType<NetworkReceiverBlockEntity> getNetworkReceiver() {
        return requireNonNull(networkReceiver).get();
    }

    public void setNetworkReceiver(final Supplier<BlockEntityType<NetworkReceiverBlockEntity>> supplier) {
        this.networkReceiver = supplier;
    }

    public BlockEntityType<NetworkTransmitterBlockEntity> getNetworkTransmitter() {
        return requireNonNull(networkTransmitter).get();
    }

    public void setNetworkTransmitter(final Supplier<BlockEntityType<NetworkTransmitterBlockEntity>> supplier) {
        this.networkTransmitter = supplier;
    }

    public BlockEntityType<AbstractPortableGridBlockEntity> getPortableGrid() {
        return requireNonNull(portableGrid).get();
    }

    public void setPortableGrid(final Supplier<BlockEntityType<AbstractPortableGridBlockEntity>> supplier) {
        this.portableGrid = supplier;
    }

    public BlockEntityType<AbstractPortableGridBlockEntity> getCreativePortableGrid() {
        return requireNonNull(creativePortableGrid).get();
    }

    public void setCreativePortableGrid(final Supplier<BlockEntityType<AbstractPortableGridBlockEntity>> supplier) {
        this.creativePortableGrid = supplier;
    }

    public BlockEntityType<SecurityManagerBlockEntity> getSecurityManager() {
        return requireNonNull(securityManager).get();
    }

    public void setSecurityManager(final Supplier<BlockEntityType<SecurityManagerBlockEntity>> supplier) {
        this.securityManager = supplier;
    }

    public BlockEntityType<RelayBlockEntity> getRelay() {
        return requireNonNull(relay).get();
    }

    public void setRelay(final Supplier<BlockEntityType<RelayBlockEntity>> supplier) {
        this.relay = supplier;
    }

    public BlockEntityType<AbstractDiskInterfaceBlockEntity> getDiskInterface() {
        return requireNonNull(diskInterface).get();
    }

    public void setDiskInterface(final Supplier<BlockEntityType<AbstractDiskInterfaceBlockEntity>> supplier) {
        this.diskInterface = supplier;
    }

    public BlockEntityType<AutocrafterBlockEntity> getAutocrafter() {
        return requireNonNull(autocrafter).get();
    }

    public void setAutocrafter(final Supplier<BlockEntityType<AutocrafterBlockEntity>> supplier) {
        this.autocrafter = supplier;
    }

    public BlockEntityType<AutocrafterManagerBlockEntity> getAutocrafterManager() {
        return requireNonNull(autocrafterManager).get();
    }

    public void setAutocrafterManager(final Supplier<BlockEntityType<AutocrafterManagerBlockEntity>> supplier) {
        this.autocrafterManager = supplier;
    }

    public BlockEntityType<AutocraftingMonitorBlockEntity> getAutocraftingMonitor() {
        return requireNonNull(autocraftingMonitor).get();
    }

    public void setAutocraftingMonitor(final Supplier<BlockEntityType<AutocraftingMonitorBlockEntity>> supplier) {
        this.autocraftingMonitor = supplier;
    }
}
