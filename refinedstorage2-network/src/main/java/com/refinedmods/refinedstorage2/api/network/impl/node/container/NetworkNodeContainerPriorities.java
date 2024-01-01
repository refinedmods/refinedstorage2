package com.refinedmods.refinedstorage2.api.network.impl.node.container;

public final class NetworkNodeContainerPriorities {
    /**
     * It is important that the grid has the highest priority when a network split or merge occurs.
     * This priority will affect the grids that are part of the a) newly created network for a split and
     * b) removed network in case of a merge.
     * For a network split, this will ensure that the grid will be able to invalidate all its watchers first and attach
     * to the newly created network.
     * After that, all the storages will be re-added into the newly created network.
     * For a network merge, this will ensure that the grid will be able to invalidate all its watchers first and attach
     * to the other existing network to merge with.
     * After that, all the storages that are part of the removed network will be re-added into the other existing
     * network to merge with.
     * The storages that were already part of the existing network aren't re-added, but those are re-synced in the
     * invalidation step.
     */
    public static final int GRID = Integer.MAX_VALUE;

    private NetworkNodeContainerPriorities() {
    }
}
