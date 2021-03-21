package com.refinedmods.refinedstorage2.fabric.block;

import alexiil.mc.lib.attributes.AttributeList;
import alexiil.mc.lib.attributes.AttributeProvider;
import com.refinedmods.refinedstorage2.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DiskDriveBlock extends NetworkNodeBlock implements AttributeProvider {
    public DiskDriveBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected boolean hasBiDirection() {
        return true;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockView world) {
        return new DiskDriveBlockEntity();
    }

    @Override
    public void addAllAttributes(World world, BlockPos blockPos, BlockState blockState, AttributeList<?> attributeList) {
        attributeList.offer(((DiskDriveBlockEntity) world.getBlockEntity(blockPos)).getDiskInventory());
    }
}