package com.refinedmods.refinedstorage2.platform.fabric.init;

import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.platform.fabric.FeatureFlag;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RelayBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.ItemGridBlockEntity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class Rs2BlockEntities {
    private BlockEntityType<CableBlockEntity> cable;
    private BlockEntityType<DiskDriveBlockEntity> diskDrive;
    private BlockEntityType<ItemGridBlockEntity> grid;
    private BlockEntityType<ControllerBlockEntity> controller;
    private BlockEntityType<ControllerBlockEntity> creativeController;
    private BlockEntityType<RelayBlockEntity> relay;

    public void register(Rs2Blocks blocks) {
        cable = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("cable"), FabricBlockEntityTypeBuilder.create(CableBlockEntity::new, blocks.getCable()).build(null));
        diskDrive = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("disk_drive"), FabricBlockEntityTypeBuilder.create(DiskDriveBlockEntity::new, blocks.getDiskDrive()).build(null));
        grid = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("grid"), FabricBlockEntityTypeBuilder.create(ItemGridBlockEntity::new, blocks.getGrid().toArray()).build(null));
        controller = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("controller"), FabricBlockEntityTypeBuilder.create((pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state), blocks.getController().toArray()).build(null));
        creativeController = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("creative_controller"), FabricBlockEntityTypeBuilder.create((pos, state) -> new ControllerBlockEntity(ControllerType.CREATIVE, pos, state), blocks.getCreativeController().toArray()).build(null));

        if (Rs2Mod.FEATURES.contains(FeatureFlag.RELAY)) {
            relay = Registry.register(Registry.BLOCK_ENTITY_TYPE, Rs2Mod.createIdentifier("relay"), FabricBlockEntityTypeBuilder.create(RelayBlockEntity::new, blocks.getRelay()).build(null));
        }
    }

    public BlockEntityType<CableBlockEntity> getCable() {
        return cable;
    }

    public BlockEntityType<DiskDriveBlockEntity> getDiskDrive() {
        return diskDrive;
    }

    public BlockEntityType<ItemGridBlockEntity> getGrid() {
        return grid;
    }

    public BlockEntityType<ControllerBlockEntity> getController() {
        return controller;
    }

    public BlockEntityType<ControllerBlockEntity> getCreativeController() {
        return creativeController;
    }

    public BlockEntityType<RelayBlockEntity> getRelay() {
        return relay;
    }
}
