package com.refinedmods.refinedstorage2.fabric.init;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.fabric.block.entity.DiskDriveBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class RefinedStorage2BlockEntities {
    private BlockEntityType<CableBlockEntity> cable;
    private BlockEntityType<DiskDriveBlockEntity> diskDrive;

    public void register(RefinedStorage2Blocks blocks) {
        cable = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(RefinedStorage2Mod.ID, "cable"), BlockEntityType.Builder.create(CableBlockEntity::new, blocks.getCable()).build(null));
        diskDrive = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(RefinedStorage2Mod.ID, "disk_drive"), BlockEntityType.Builder.create(DiskDriveBlockEntity::new, blocks.getDiskDrive()).build(null));
    }

    public BlockEntityType<CableBlockEntity> getCable() {
        return cable;
    }

    public BlockEntityType<DiskDriveBlockEntity> getDiskDrive() {
        return diskDrive;
    }
}
