package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.api.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.GridSize;
import com.refinedmods.refinedstorage2.api.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class GridBlockEntity extends NetworkNodeBlockEntity<GridNetworkNode> implements ExtendedScreenHandlerFactory {
    private static final String TAG_SORTING_DIRECTION = "sd";
    private static final String TAG_SORTING_TYPE = "st";
    private static final String TAG_SIZE = "s";
    private static final String TAG_SEARCH_BOX_MODE = "sbm";

    public GridBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getGrid(), pos, state);
    }

    @Override
    protected GridNetworkNode createNode(BlockPos pos, NbtCompound tag) {
        GridNetworkNode grid = new GridNetworkNode(
                Positions.ofBlockPos(pos),
                GridSearchBoxModeRegistry.INSTANCE.getDefault(),
                Rs2Config.get().getGrid().getEnergyUsage()
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
    public Text getDisplayName() {
        return Rs2Mod.createTranslation("block", "grid");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new GridScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putInt(TAG_SORTING_DIRECTION, GridSettings.getSortingDirection(container.getNode().getSortingDirection()));
        tag.putInt(TAG_SORTING_TYPE, GridSettings.getSortingType(container.getNode().getSortingType()));
        tag.putInt(TAG_SIZE, GridSettings.getSize(container.getNode().getSize()));
        tag.putInt(TAG_SEARCH_BOX_MODE, GridSearchBoxModeRegistry.INSTANCE.getId(container.getNode().getSearchBoxMode()));
        return super.writeNbt(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBoolean(container.getNode().isActive());
        buf.writeInt(GridSettings.getSortingDirection(getSortingDirection()));
        buf.writeInt(GridSettings.getSortingType(getSortingType()));
        buf.writeInt(GridSettings.getSize(getSize()));
        buf.writeInt(GridSearchBoxModeRegistry.INSTANCE.getId(getSearchBoxMode()));

        buf.writeInt(container.getNode().getStackCount());
        container.getNode().forEachStack((stack, trackerEntry) -> {
            PacketUtil.writeItemStack(buf, stack, true);
            PacketUtil.writeTrackerEntry(buf, trackerEntry);
        });
    }

    public GridSortingType getSortingType() {
        return container.getNode().getSortingType();
    }

    public void setSortingType(GridSortingType sortingType) {
        container.getNode().setSortingType(sortingType);
        markDirty();
    }

    public GridSortingDirection getSortingDirection() {
        return container.getNode().getSortingDirection();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        container.getNode().setSortingDirection(sortingDirection);
        markDirty();
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return container.getNode().getSearchBoxMode();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        container.getNode().setSearchBoxMode(searchBoxMode);
        markDirty();
    }

    public GridSize getSize() {
        return container.getNode().getSize();
    }

    public void setSize(GridSize size) {
        container.getNode().setSize(size);
        markDirty();
    }

    public void addWatcher(GridEventHandler eventHandler) {
        container.getNode().addWatcher(eventHandler);
    }

    public void removeWatcher(GridEventHandler eventHandler) {
        container.getNode().removeWatcher(eventHandler);
    }
}
