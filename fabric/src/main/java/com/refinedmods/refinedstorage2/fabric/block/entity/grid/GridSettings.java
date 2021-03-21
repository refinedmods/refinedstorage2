package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;

public class GridSettings {
    private static final int SORTING_ASCENDING = 0;
    private static final int SORTING_DESCENDING = 1;

    private static final int SORTING_TYPE_QUANTITY = 0;
    private static final int SORTING_TYPE_NAME = 1;
    private static final int SORTING_TYPE_ID = 2;
    private static final int SORTING_TYPE_LAST_MODIFIED = 3;

    public static GridSortingDirection getSortingDirection(int sortingDirection) {
        switch (sortingDirection) {
            case SORTING_ASCENDING:
                return GridSortingDirection.ASCENDING;
            case SORTING_DESCENDING:
                return GridSortingDirection.DESCENDING;
            default:
                return GridSortingDirection.ASCENDING;
        }
    }

    public static int getSortingDirection(GridSortingDirection sortingDirection) {
        switch (sortingDirection) {
            case ASCENDING:
                return SORTING_ASCENDING;
            case DESCENDING:
                return SORTING_DESCENDING;
            default:
                return SORTING_ASCENDING;
        }
    }

    public static GridSortingType getSortingType(int sortingType) {
        switch (sortingType) {
            case SORTING_TYPE_QUANTITY:
                return GridSortingType.QUANTITY;
            case SORTING_TYPE_NAME:
                return GridSortingType.NAME;
            case SORTING_TYPE_ID:
                return GridSortingType.ID;
            case SORTING_TYPE_LAST_MODIFIED:
                return GridSortingType.LAST_MODIFIED;
            default:
                return GridSortingType.QUANTITY;
        }
    }

    public static int getSortingType(GridSortingType sortingType) {
        switch (sortingType) {
            case QUANTITY:
                return SORTING_TYPE_QUANTITY;
            case NAME:
                return SORTING_TYPE_NAME;
            case ID:
                return SORTING_TYPE_ID;
            case LAST_MODIFIED:
                return SORTING_TYPE_LAST_MODIFIED;
            default:
                return SORTING_TYPE_QUANTITY;
        }
    }
}
