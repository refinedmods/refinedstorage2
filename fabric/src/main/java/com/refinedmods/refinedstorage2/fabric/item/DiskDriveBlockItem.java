package com.refinedmods.refinedstorage2.fabric.item;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Direction;

public class DiskDriveBlockItem extends BlockItem {
    public DiskDriveBlockItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public ActionResult place(ItemPlacementContext context) {
        System.out.println("Place");
        Direction horizontalDirection = context.getPlayerFacing().getOpposite();
        Direction verticalDirection = Direction.UP;

        if (context.getPlayer().pitch > 65) {
            verticalDirection = horizontalDirection.getOpposite();
            horizontalDirection = Direction.UP;
        } else if (context.getPlayer().pitch < -65) {
            verticalDirection = horizontalDirection.getOpposite();
            horizontalDirection = Direction.DOWN;
        }

        System.out.println("Horizontal Direction: " + horizontalDirection);
        System.out.println("Vertical Direction: " + verticalDirection);

        return super.place(context);
    }
}
