package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.core.grid.GridSize;
import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GridBlockEntity extends NetworkNodeBlockEntity<GridNetworkNode> implements ExtendedScreenHandlerFactory {
    private static final String TAG_SORTING_DIRECTION = "sd";
    private static final String TAG_SORTING_TYPE = "st";
    private static final String TAG_SIZE = "s";
    private static final String TAG_SEARCH_BOX_MODE = "sbm";

    public GridBlockEntity() {
        super(Rs2Mod.BLOCK_ENTITIES.getGrid());
    }

    @Override
    protected GridNetworkNode createNode(World world, BlockPos pos, CompoundTag tag) {
        GridNetworkNode grid = new GridNetworkNode(
                FabricRs2WorldAdapter.of(world),
                Positions.ofBlockPos(pos),
                Rs2Mod.API.getGridSearchBoxModeRegistry(),
                Rs2Config.get().getGrid().getEnergyUsage()
        );

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
            grid.setSearchBoxMode(Rs2Mod.API.getGridSearchBoxModeRegistry().get(tag.getInt(TAG_SEARCH_BOX_MODE)));
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
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt(TAG_SORTING_DIRECTION, GridSettings.getSortingDirection(host.getNode().getSortingDirection()));
        tag.putInt(TAG_SORTING_TYPE, GridSettings.getSortingType(host.getNode().getSortingType()));
        tag.putInt(TAG_SIZE, GridSettings.getSize(host.getNode().getSize()));
        tag.putInt(TAG_SEARCH_BOX_MODE, Rs2Mod.API.getGridSearchBoxModeRegistry().getId(host.getNode().getSearchBoxMode()));

        return super.toTag(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBoolean(host.getNode().isActive());
        buf.writeInt(GridSettings.getSortingDirection(getSortingDirection()));
        buf.writeInt(GridSettings.getSortingType(getSortingType()));
        buf.writeInt(GridSettings.getSize(getSize()));
        buf.writeInt(Rs2Mod.API.getGridSearchBoxModeRegistry().getId(getSearchBoxMode()));

        buf.writeInt(host.getNode().getStackCount());
        host.getNode().forEachStack((stack, trackerEntry) -> {
            PacketUtil.writeItemStack(buf, stack, true);
            PacketUtil.writeTrackerEntry(buf, trackerEntry);
        });
    }

    public GridSortingType getSortingType() {
        return host.getNode().getSortingType();
    }

    public void setSortingType(GridSortingType sortingType) {
        host.getNode().setSortingType(sortingType);
        markDirty();
    }

    public GridSortingDirection getSortingDirection() {
        return host.getNode().getSortingDirection();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        host.getNode().setSortingDirection(sortingDirection);
        markDirty();
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return host.getNode().getSearchBoxMode();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        host.getNode().setSearchBoxMode(searchBoxMode);
        markDirty();
    }

    public GridSize getSize() {
        return host.getNode().getSize();
    }

    public void setSize(GridSize size) {
        host.getNode().setSize(size);
        markDirty();
    }

    public void addWatcher(GridEventHandler eventHandler) {
        host.getNode().addWatcher(eventHandler);
    }

    public void removeWatcher(GridEventHandler eventHandler) {
        host.getNode().removeWatcher(eventHandler);
    }
}
