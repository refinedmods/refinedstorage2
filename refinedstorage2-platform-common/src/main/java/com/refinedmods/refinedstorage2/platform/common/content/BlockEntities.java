package com.refinedmods.refinedstorage2.platform.common.content;

import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.common.constructordestructor.ConstructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.constructordestructor.DestructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.controller.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.detector.DetectorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.exporter.ExporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.grid.CraftingGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.importer.ImporterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkReceiverBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.networking.NetworkTransmitterBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.externalstorage.ExternalStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.portablegrid.AbstractPortableGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.storageblock.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storage.storageblock.ItemStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.storagemonitor.StorageMonitorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.network.NetworkNodeContainerBlockEntityImpl;
import com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter.WirelessTransmitterBlockEntity;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.world.level.block.entity.BlockEntityType;

import static java.util.Objects.requireNonNull;

public final class BlockEntities {
    public static final BlockEntities INSTANCE = new BlockEntities();

    @Nullable
    private Supplier<BlockEntityType<NetworkNodeContainerBlockEntityImpl<SimpleNetworkNode>>> cable;
    @Nullable
    private Supplier<BlockEntityType<? extends AbstractDiskDriveBlockEntity>> diskDrive;
    @Nullable
    private Supplier<BlockEntityType<GridBlockEntity>> grid;
    @Nullable
    private Supplier<BlockEntityType<CraftingGridBlockEntity>> craftingGrid;
    @Nullable
    private Supplier<BlockEntityType<ControllerBlockEntity>> controller;
    @Nullable
    private Supplier<BlockEntityType<ControllerBlockEntity>> creativeController;
    private final Map<ItemStorageType.Variant, Supplier<BlockEntityType<ItemStorageBlockBlockEntity>>>
        itemStorageBlocks = new EnumMap<>(ItemStorageType.Variant.class);
    private final Map<FluidStorageType.Variant, Supplier<BlockEntityType<FluidStorageBlockBlockEntity>>>
        fluidStorageBlocks = new EnumMap<>(FluidStorageType.Variant.class);
    @Nullable
    private Supplier<BlockEntityType<ImporterBlockEntity>> importer;
    @Nullable
    private Supplier<BlockEntityType<ExporterBlockEntity>> exporter;
    @Nullable
    private Supplier<BlockEntityType<InterfaceBlockEntity>> iface;
    @Nullable
    private Supplier<BlockEntityType<ExternalStorageBlockEntity>> externalStorage;
    @Nullable
    private Supplier<BlockEntityType<DetectorBlockEntity>> detector;
    @Nullable
    private Supplier<BlockEntityType<DestructorBlockEntity>> destructor;
    @Nullable
    private Supplier<BlockEntityType<ConstructorBlockEntity>> constructor;
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

    private BlockEntities() {
    }

    public BlockEntityType<NetworkNodeContainerBlockEntityImpl<SimpleNetworkNode>> getCable() {
        return requireNonNull(cable).get();
    }

    public void setCable(
        final Supplier<BlockEntityType<NetworkNodeContainerBlockEntityImpl<SimpleNetworkNode>>> supplier
    ) {
        this.cable = supplier;
    }

    public BlockEntityType<? extends AbstractDiskDriveBlockEntity> getDiskDrive() {
        return requireNonNull(diskDrive).get();
    }

    public void setDiskDrive(final Supplier<BlockEntityType<? extends AbstractDiskDriveBlockEntity>> supplier) {
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

    public void setItemStorageBlock(final ItemStorageType.Variant variant,
                                    final Supplier<BlockEntityType<ItemStorageBlockBlockEntity>> supplier) {
        itemStorageBlocks.put(variant, supplier);
    }

    public BlockEntityType<ItemStorageBlockBlockEntity> getItemStorageBlock(final ItemStorageType.Variant variant) {
        return itemStorageBlocks.get(variant).get();
    }

    public void setFluidStorageBlock(final FluidStorageType.Variant variant,
                                     final Supplier<BlockEntityType<FluidStorageBlockBlockEntity>> supplier) {
        fluidStorageBlocks.put(variant, supplier);
    }

    public BlockEntityType<FluidStorageBlockBlockEntity> getFluidStorageBlock(final FluidStorageType.Variant variant) {
        return fluidStorageBlocks.get(variant).get();
    }

    public BlockEntityType<ImporterBlockEntity> getImporter() {
        return requireNonNull(importer).get();
    }

    public void setImporter(final Supplier<BlockEntityType<ImporterBlockEntity>> supplier) {
        this.importer = supplier;
    }

    public BlockEntityType<ExporterBlockEntity> getExporter() {
        return requireNonNull(exporter).get();
    }

    public void setExporter(final Supplier<BlockEntityType<ExporterBlockEntity>> supplier) {
        this.exporter = supplier;
    }

    public BlockEntityType<InterfaceBlockEntity> getInterface() {
        return requireNonNull(iface).get();
    }

    public void setInterface(final Supplier<BlockEntityType<InterfaceBlockEntity>> supplier) {
        this.iface = supplier;
    }

    public BlockEntityType<ExternalStorageBlockEntity> getExternalStorage() {
        return requireNonNull(externalStorage).get();
    }

    public void setExternalStorage(final Supplier<BlockEntityType<ExternalStorageBlockEntity>> supplier) {
        this.externalStorage = supplier;
    }

    public BlockEntityType<DetectorBlockEntity> getDetector() {
        return requireNonNull(detector).get();
    }

    public void setDetector(final Supplier<BlockEntityType<DetectorBlockEntity>> supplier) {
        this.detector = supplier;
    }

    public BlockEntityType<DestructorBlockEntity> getDestructor() {
        return requireNonNull(destructor).get();
    }

    public void setDestructor(final Supplier<BlockEntityType<DestructorBlockEntity>> supplier) {
        this.destructor = supplier;
    }

    public BlockEntityType<ConstructorBlockEntity> getConstructor() {
        return requireNonNull(constructor).get();
    }

    public void setConstructor(final Supplier<BlockEntityType<ConstructorBlockEntity>> supplier) {
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
}
