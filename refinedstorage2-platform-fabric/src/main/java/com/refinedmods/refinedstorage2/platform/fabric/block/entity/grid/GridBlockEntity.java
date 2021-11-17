package com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.FabricNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class GridBlockEntity<T> extends FabricNetworkNodeContainerBlockEntity<GridNetworkNode<T>> implements ExtendedScreenHandlerFactory {
    private static final String TAG_SORTING_DIRECTION = "sd";
    private static final String TAG_SORTING_TYPE = "st";
    private static final String TAG_SIZE = "s";
    private static final String TAG_SEARCH_BOX_MODE = "sbm";

    private final StorageChannelType<T> type;

    public GridBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state, StorageChannelType<T> type) {
        super(blockEntityType, pos, state);
        this.type = type;
    }

    @Override
    protected GridNetworkNode<T> createNode(BlockPos pos, CompoundTag tag) {
        GridNetworkNode<T> grid = new GridNetworkNode<>(
                GridSearchBoxModeRegistry.INSTANCE.getDefault(),
                Rs2Config.get().getGrid().getEnergyUsage(),
                type
        );

        if (tag != null) {
            if (tag.contains(TAG_SORTING_DIRECTION)) {
                grid.setSortingDirection(GridSettings.getSortingDirection(tag.getInt(TAG_SORTING_DIRECTION)));
            }

            if (tag.contains(TAG_SORTING_TYPE)) {
                grid.setSortingType(GridSettings.getSortingType(tag.getInt(TAG_SORTING_TYPE)));
            }

            if (tag.contains(TAG_SIZE)) {
                grid.setSize(GridSettings.getSize(tag.getInt(TAG_SIZE)));
            }

            if (tag.contains(TAG_SEARCH_BOX_MODE)) {
                grid.setSearchBoxMode(GridSearchBoxModeRegistry.INSTANCE.get(tag.getInt(TAG_SEARCH_BOX_MODE)));
            }
        }

        return grid;
    }

    @Override
    public Component getDisplayName() {
        return Rs2Mod.createTranslation("block", "grid");
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt(TAG_SORTING_DIRECTION, GridSettings.getSortingDirection(getContainer().getNode().getSortingDirection()));
        tag.putInt(TAG_SORTING_TYPE, GridSettings.getSortingType(getContainer().getNode().getSortingType()));
        tag.putInt(TAG_SIZE, GridSettings.getSize(getContainer().getNode().getSize()));
        tag.putInt(TAG_SEARCH_BOX_MODE, GridSearchBoxModeRegistry.INSTANCE.getId(getContainer().getNode().getSearchBoxMode()));
        return super.save(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBoolean(getContainer().getNode().isActive());
        buf.writeInt(GridSettings.getSortingDirection(getSortingDirection()));
        buf.writeInt(GridSettings.getSortingType(getSortingType()));
        buf.writeInt(GridSettings.getSize(getSize()));
        buf.writeInt(GridSearchBoxModeRegistry.INSTANCE.getId(getSearchBoxMode()));

        buf.writeInt(getContainer().getNode().getResourceCount());
        getContainer().getNode().forEachResource((stack, trackerEntry) -> {
            writeResourceAmount(buf, stack);
            PacketUtil.writeTrackerEntry(buf, trackerEntry);
        });
    }

    protected abstract void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<T> stack);

    public GridSortingType getSortingType() {
        return getContainer().getNode().getSortingType();
    }

    public void setSortingType(GridSortingType sortingType) {
        getContainer().getNode().setSortingType(sortingType);
        setChanged();
    }

    public GridSortingDirection getSortingDirection() {
        return getContainer().getNode().getSortingDirection();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        getContainer().getNode().setSortingDirection(sortingDirection);
        setChanged();
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return getContainer().getNode().getSearchBoxMode();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        getContainer().getNode().setSearchBoxMode(searchBoxMode);
        setChanged();
    }

    public GridSize getSize() {
        return getContainer().getNode().getSize();
    }

    public void setSize(GridSize size) {
        getContainer().getNode().setSize(size);
        setChanged();
    }

    public void addWatcher(GridWatcher watcher) {
        getContainer().getNode().addWatcher(watcher);
    }

    public void removeWatcher(GridWatcher watcher) {
        getContainer().getNode().removeWatcher(watcher);
    }
}
