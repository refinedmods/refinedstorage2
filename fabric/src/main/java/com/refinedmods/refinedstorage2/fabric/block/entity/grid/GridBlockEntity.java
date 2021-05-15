package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.core.grid.GridSize;
import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricRs2WorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.fabric.util.Positions;

import java.util.Collection;

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
                FabricNetworkNodeReference.of(world, pos),
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
        tag.putInt(TAG_SORTING_DIRECTION, GridSettings.getSortingDirection(node.getSortingDirection()));
        tag.putInt(TAG_SORTING_TYPE, GridSettings.getSortingType(node.getSortingType()));
        tag.putInt(TAG_SIZE, GridSettings.getSize(node.getSize()));
        tag.putInt(TAG_SEARCH_BOX_MODE, Rs2Mod.API.getGridSearchBoxModeRegistry().getId(node.getSearchBoxMode()));

        return super.toTag(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBoolean(isActive());
        buf.writeInt(GridSettings.getSortingDirection(getSortingDirection()));
        buf.writeInt(GridSettings.getSortingType(getSortingType()));
        buf.writeInt(GridSettings.getSize(getSize()));
        buf.writeInt(Rs2Mod.API.getGridSearchBoxModeRegistry().getId(getSearchBoxMode()));

        Collection<Rs2ItemStack> stacks = getNetwork().getItemStorageChannel().getStacks();

        buf.writeInt(stacks.size());
        stacks.forEach(stack -> {
            PacketUtil.writeItemStack(buf, stack, true);
            PacketUtil.writeTrackerEntry(buf, getNetwork().getItemStorageChannel().getTracker().getEntry(stack));
        });
    }

    public GridSortingType getSortingType() {
        return node.getSortingType();
    }

    public void setSortingType(GridSortingType sortingType) {
        node.setSortingType(sortingType);
        markDirty();
    }

    public GridSortingDirection getSortingDirection() {
        return node.getSortingDirection();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        node.setSortingDirection(sortingDirection);
        markDirty();
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return node.getSearchBoxMode();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        node.setSearchBoxMode(searchBoxMode);
        markDirty();
    }

    public GridSize getSize() {
        return node.getSize();
    }

    public void setSize(GridSize size) {
        node.setSize(size);
        markDirty();
    }

    public void addWatcher(GridEventHandler eventHandler) {
        node.addWatcher(eventHandler);
    }

    public void removeWatcher(GridEventHandler eventHandler) {
        node.removeWatcher(eventHandler);
    }
}
