package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.core.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class Rs2BlockEntities {
    private BlockEntityType<CableBlockEntity> cable;
    private BlockEntityType<DiskDriveBlockEntity> diskDrive;
    private BlockEntityType<GridBlockEntity> grid;
    private BlockEntityType<ControllerBlockEntity> controller;
    private BlockEntityType<ControllerBlockEntity> creativeController;

    public void register(Rs2Blocks blocks) {
        cable = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("cable"), BlockEntityType.Builder.create(CableBlockEntity::new, blocks.getCable()).build(null));
        diskDrive = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("disk_drive"), BlockEntityType.Builder.create(DiskDriveBlockEntity::new, blocks.getDiskDrive()).build(null));
        grid = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("grid"), BlockEntityType.Builder.create(GridBlockEntity::new, blocks.getGrid().toArray()).build(null));
        controller = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("controller"), BlockEntityType.Builder.create(() -> new ControllerBlockEntity(ControllerType.NORMAL), blocks.getController().toArray()).build(null));
        creativeController = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("creative_controller"), BlockEntityType.Builder.create(() -> new ControllerBlockEntity(ControllerType.CREATIVE), blocks.getCreativeController().toArray()).build(null));
    }

    public BlockEntityType<CableBlockEntity> getCable() {
        return cable;
    }

    public BlockEntityType<DiskDriveBlockEntity> getDiskDrive() {
        return diskDrive;
    }

    public BlockEntityType<GridBlockEntity> getGrid() {
        return grid;
    }

    public BlockEntityType<ControllerBlockEntity> getController() {
        return controller;
    }

    public BlockEntityType<ControllerBlockEntity> getCreativeController() {
        return creativeController;
    }
}
