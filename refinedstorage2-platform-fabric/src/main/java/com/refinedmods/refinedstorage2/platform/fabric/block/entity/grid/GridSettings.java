package com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.grid.GridSize;
import com.refinedmods.refinedstorage2.api.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.GridSortingType;

public class GridSettings {
    private static final int SORTING_ASCENDING = 0;
    private static final int SORTING_DESCENDING = 1;

    private static final int SORTING_TYPE_QUANTITY = 0;
    private static final int SORTING_TYPE_NAME = 1;
    private static final int SORTING_TYPE_ID = 2;
    private static final int SORTING_TYPE_LAST_MODIFIED = 3;

    private static final int SIZE_STRETCH = 0;
    private static final int SIZE_SMALL = 1;
    private static final int SIZE_MEDIUM = 2;
    private static final int SIZE_LARGE = 3;
    private static final int SIZE_EXTRA_LARGE = 4;

    private GridSettings() {
    }

    public static GridSortingDirection getSortingDirection(int sortingDirection) {
        return switch (sortingDirection) {
            case SORTING_ASCENDING -> GridSortingDirection.ASCENDING;
            case SORTING_DESCENDING -> GridSortingDirection.DESCENDING;
            default -> GridSortingDirection.ASCENDING;
        };
    }

    public static int getSortingDirection(GridSortingDirection sortingDirection) {
        return switch (sortingDirection) {
            case ASCENDING -> SORTING_ASCENDING;
            case DESCENDING -> SORTING_DESCENDING;
        };
    }

    public static GridSortingType getSortingType(int sortingType) {
        return switch (sortingType) {
            case SORTING_TYPE_QUANTITY -> GridSortingType.QUANTITY;
            case SORTING_TYPE_NAME -> GridSortingType.NAME;
            case SORTING_TYPE_ID -> GridSortingType.ID;
            case SORTING_TYPE_LAST_MODIFIED -> GridSortingType.LAST_MODIFIED;
            default -> GridSortingType.QUANTITY;
        };
    }

    public static int getSortingType(GridSortingType sortingType) {
        return switch (sortingType) {
            case QUANTITY -> SORTING_TYPE_QUANTITY;
            case NAME -> SORTING_TYPE_NAME;
            case ID -> SORTING_TYPE_ID;
            case LAST_MODIFIED -> SORTING_TYPE_LAST_MODIFIED;
        };
    }

    public static GridSize getSize(int size) {
        return switch (size) {
            case SIZE_STRETCH -> GridSize.STRETCH;
            case SIZE_SMALL -> GridSize.SMALL;
            case SIZE_MEDIUM -> GridSize.MEDIUM;
            case SIZE_LARGE -> GridSize.LARGE;
            case SIZE_EXTRA_LARGE -> GridSize.EXTRA_LARGE;
            default -> GridSize.STRETCH;
        };
    }

    public static int getSize(GridSize size) {
        return switch (size) {
            case STRETCH -> SIZE_STRETCH;
            case SMALL -> SIZE_SMALL;
            case MEDIUM -> SIZE_MEDIUM;
            case LARGE -> SIZE_LARGE;
            case EXTRA_LARGE -> SIZE_EXTRA_LARGE;
        };
    }
}
