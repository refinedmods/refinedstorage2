package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.grid.GridBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2BlockEntities {
    private BlockEntityType<CableBlockEntity> cable;
    private BlockEntityType<DiskDriveBlockEntity> diskDrive;
    private BlockEntityType<GridBlockEntity> grid;

    public void register(RefinedStorage2Blocks blocks) {
        cable = Registry.register(Registry.BLOCK_ENTITY_TYPE, RefinedStorage2Mod.createIdentifier("cable"), BlockEntityType.Builder.create(CableBlockEntity::new, blocks.getCable()).build(null));
        diskDrive = Registry.register(Registry.BLOCK_ENTITY_TYPE, RefinedStorage2Mod.createIdentifier("disk_drive"), BlockEntityType.Builder.create(DiskDriveBlockEntity::new, blocks.getDiskDrive()).build(null));
        grid = Registry.register(Registry.BLOCK_ENTITY_TYPE, RefinedStorage2Mod.createIdentifier("grid"), BlockEntityType.Builder.create(GridBlockEntity::new, blocks.getGrid().toArray()).build(null));
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
}
