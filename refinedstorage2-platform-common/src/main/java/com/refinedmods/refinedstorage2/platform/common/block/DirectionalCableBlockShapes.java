package com.refinedmods.refinedstorage2.platform.common.block;

import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.Block.box;

final class DirectionalCableBlockShapes {
    private static final VoxelShape EXPORTER_NORTH_1 = box(6, 6, 0, 10, 10, 2);
    private static final VoxelShape EXPORTER_NORTH_2 = box(5, 5, 2, 11, 11, 4);
    private static final VoxelShape EXPORTER_NORTH_3 = box(3, 3, 4, 13, 13, 6);

    private static final VoxelShape EXPORTER_EAST_1 = box(14, 6, 6, 16, 10, 10);
    private static final VoxelShape EXPORTER_EAST_2 = box(12, 5, 5, 14, 11, 11);
    private static final VoxelShape EXPORTER_EAST_3 = box(10, 3, 3, 12, 13, 13);

    private static final VoxelShape EXPORTER_SOUTH_1 = box(6, 6, 14, 10, 10, 16);
    private static final VoxelShape EXPORTER_SOUTH_2 = box(5, 5, 12, 11, 11, 14);
    private static final VoxelShape EXPORTER_SOUTH_3 = box(3, 3, 10, 13, 13, 12);

    private static final VoxelShape EXPORTER_WEST_1 = box(0, 6, 6, 2, 10, 10);
    private static final VoxelShape EXPORTER_WEST_2 = box(2, 5, 5, 4, 11, 11);
    private static final VoxelShape EXPORTER_WEST_3 = box(4, 3, 3, 6, 13, 13);

    private static final VoxelShape EXPORTER_UP_1 = box(6, 14, 6, 10, 16, 10);
    private static final VoxelShape EXPORTER_UP_2 = box(5, 12, 5, 11, 14, 11);
    private static final VoxelShape EXPORTER_UP_3 = box(3, 10, 3, 13, 12, 13);

    private static final VoxelShape EXPORTER_DOWN_1 = box(6, 0, 6, 10, 2, 10);
    private static final VoxelShape EXPORTER_DOWN_2 = box(5, 2, 5, 11, 4, 11);
    private static final VoxelShape EXPORTER_DOWN_3 = box(3, 4, 3, 13, 6, 13);

    private static final VoxelShape IMPORTER_NORTH_1 = box(6, 6, 4, 10, 10, 6);
    private static final VoxelShape IMPORTER_NORTH_2 = box(5, 5, 2, 11, 11, 4);
    private static final VoxelShape IMPORTER_NORTH_3 = box(3, 3, 0, 13, 13, 2);

    private static final VoxelShape IMPORTER_EAST_1 = box(10, 6, 6, 12, 10, 10);
    private static final VoxelShape IMPORTER_EAST_2 = box(12, 5, 5, 14, 11, 11);
    private static final VoxelShape IMPORTER_EAST_3 = box(14, 3, 3, 16, 13, 13);

    private static final VoxelShape IMPORTER_SOUTH_1 = box(6, 6, 10, 10, 10, 12);
    private static final VoxelShape IMPORTER_SOUTH_2 = box(5, 5, 12, 11, 11, 14);
    private static final VoxelShape IMPORTER_SOUTH_3 = box(3, 3, 14, 13, 13, 16);

    private static final VoxelShape IMPORTER_WEST_1 = box(4, 6, 6, 6, 10, 10);
    private static final VoxelShape IMPORTER_WEST_2 = box(2, 5, 5, 4, 11, 11);
    private static final VoxelShape IMPORTER_WEST_3 = box(0, 3, 3, 2, 13, 13);

    private static final VoxelShape IMPORTER_UP_1 = box(6, 10, 6, 10, 12, 10);
    private static final VoxelShape IMPORTER_UP_2 = box(5, 12, 5, 11, 14, 11);
    private static final VoxelShape IMPORTER_UP_3 = box(3, 14, 3, 13, 16, 13);

    private static final VoxelShape IMPORTER_DOWN_1 = box(6, 4, 6, 10, 6, 10);
    private static final VoxelShape IMPORTER_DOWN_2 = box(5, 2, 5, 11, 4, 11);
    private static final VoxelShape IMPORTER_DOWN_3 = box(3, 0, 3, 13, 2, 13);

    static final VoxelShape EXPORTER_NORTH = Shapes.or(EXPORTER_NORTH_1, EXPORTER_NORTH_2, EXPORTER_NORTH_3);
    static final VoxelShape EXPORTER_EAST = Shapes.or(EXPORTER_EAST_1, EXPORTER_EAST_2, EXPORTER_EAST_3);
    static final VoxelShape EXPORTER_SOUTH = Shapes.or(EXPORTER_SOUTH_1, EXPORTER_SOUTH_2, EXPORTER_SOUTH_3);
    static final VoxelShape EXPORTER_WEST = Shapes.or(EXPORTER_WEST_1, EXPORTER_WEST_2, EXPORTER_WEST_3);
    static final VoxelShape EXPORTER_UP = Shapes.or(EXPORTER_UP_1, EXPORTER_UP_2, EXPORTER_UP_3);
    static final VoxelShape EXPORTER_DOWN = Shapes.or(EXPORTER_DOWN_1, EXPORTER_DOWN_2, EXPORTER_DOWN_3);

    static final VoxelShape IMPORTER_NORTH = Shapes.or(IMPORTER_NORTH_1, IMPORTER_NORTH_2, IMPORTER_NORTH_3);
    static final VoxelShape IMPORTER_EAST = Shapes.or(IMPORTER_EAST_1, IMPORTER_EAST_2, IMPORTER_EAST_3);
    static final VoxelShape IMPORTER_SOUTH = Shapes.or(IMPORTER_SOUTH_1, IMPORTER_SOUTH_2, IMPORTER_SOUTH_3);
    static final VoxelShape IMPORTER_WEST = Shapes.or(IMPORTER_WEST_1, IMPORTER_WEST_2, IMPORTER_WEST_3);
    static final VoxelShape IMPORTER_UP = Shapes.or(IMPORTER_UP_1, IMPORTER_UP_2, IMPORTER_UP_3);
    static final VoxelShape IMPORTER_DOWN = Shapes.or(IMPORTER_DOWN_1, IMPORTER_DOWN_2, IMPORTER_DOWN_3);

    private DirectionalCableBlockShapes() {
    }
}
