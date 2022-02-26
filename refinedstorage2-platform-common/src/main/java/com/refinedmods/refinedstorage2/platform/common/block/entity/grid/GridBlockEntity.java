package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.abstractions.menu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.block.entity.InternalNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public abstract class GridBlockEntity<T> extends InternalNetworkNodeContainerBlockEntity<GridNetworkNode<T>> implements ExtendedMenuProvider {
    private static final String TAG_SORTING_DIRECTION = "sd";
    private static final String TAG_SORTING_TYPE = "st";
    private static final String TAG_SIZE = "s";

    protected GridBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, StorageChannelType<T> storageChannelType) {
        super(type, pos, state, new GridNetworkNode<>(
                Platform.INSTANCE.getConfig().getGrid().getEnergyUsage(),
                storageChannelType
        ));
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "grid");
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_SORTING_DIRECTION, GridSettings.getSortingDirection(getNode().getSortingDirection()));
        tag.putInt(TAG_SORTING_TYPE, GridSettings.getSortingType(getNode().getSortingType()));
        tag.putInt(TAG_SIZE, GridSettings.getSize(getNode().getSize()));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_SORTING_DIRECTION)) {
            getNode().setSortingDirection(GridSettings.getSortingDirection(tag.getInt(TAG_SORTING_DIRECTION)));
        }

        if (tag.contains(TAG_SORTING_TYPE)) {
            getNode().setSortingType(GridSettings.getSortingType(tag.getInt(TAG_SORTING_TYPE)));
        }

        if (tag.contains(TAG_SIZE)) {
            getNode().setSize(GridSettings.getSize(tag.getInt(TAG_SIZE)));
        }
    }

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBoolean(getNode().isActive());
        buf.writeInt(GridSettings.getSortingDirection(getSortingDirection()));
        buf.writeInt(GridSettings.getSortingType(getSortingType()));
        buf.writeInt(GridSettings.getSize(getSize()));

        buf.writeInt(getNode().getResourceCount());
        getNode().forEachResource((stack, trackerEntry) -> {
            writeResourceAmount(buf, stack);
            PacketUtil.writeTrackerEntry(buf, trackerEntry.orElse(null));
        });
    }

    protected abstract void writeResourceAmount(FriendlyByteBuf buf, ResourceAmount<T> stack);

    public GridSortingType getSortingType() {
        return getNode().getSortingType();
    }

    public void setSortingType(GridSortingType sortingType) {
        getNode().setSortingType(sortingType);
        setChanged();
    }

    public GridSortingDirection getSortingDirection() {
        return getNode().getSortingDirection();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        getNode().setSortingDirection(sortingDirection);
        setChanged();
    }

    public GridSize getSize() {
        return getNode().getSize();
    }

    public void setSize(GridSize size) {
        getNode().setSize(size);
        setChanged();
    }

    public void addWatcher(GridWatcher watcher) {
        getNode().addWatcher(watcher);
    }

    public void removeWatcher(GridWatcher watcher) {
        getNode().removeWatcher(watcher);
    }
}
