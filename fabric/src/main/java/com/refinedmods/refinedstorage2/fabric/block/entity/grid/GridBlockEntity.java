package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import java.util.Collection;

import com.refinedmods.refinedstorage2.core.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.core.grid.GridSize;
import com.refinedmods.refinedstorage2.core.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.core.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.adapter.FabricWorldAdapter;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;
import com.refinedmods.refinedstorage2.fabric.util.PacketUtil;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GridBlockEntity extends NetworkNodeBlockEntity<GridNetworkNode> implements ExtendedScreenHandlerFactory {
    public GridBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getGrid());
    }

    @Override
    protected GridNetworkNode createNode(World world, BlockPos pos) {
        return new GridNetworkNode(
            FabricWorldAdapter.of(world),
            pos,
            FabricNetworkNodeReference.of(world, pos),
            RefinedStorage2Mod.API.getGridSearchBoxModeRegistry()
        );
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.refinedstorage2.grid");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new GridScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void fromTag(BlockState blockState, CompoundTag tag) {
        if (tag.contains("sd")) {
            node.setSortingDirection(GridSettings.getSortingDirection(tag.getInt("sd")));
        }

        if (tag.contains("st")) {
            node.setSortingType(GridSettings.getSortingType(tag.getInt("st")));
        }

        if (tag.contains("s")) {
            node.setSize(GridSettings.getSize(tag.getInt("s")));
        }

        if (tag.contains("sbm")) {
            node.setSearchBoxMode(RefinedStorage2Mod.API.getGridSearchBoxModeRegistry().get(tag.getInt("sbm")));
        }

        super.fromTag(blockState, tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("sd", GridSettings.getSortingDirection(node.getSortingDirection()));
        tag.putInt("st", GridSettings.getSortingType(node.getSortingType()));
        tag.putInt("s", GridSettings.getSize(node.getSize()));
        tag.putInt("sbm", RefinedStorage2Mod.API.getGridSearchBoxModeRegistry().getId(node.getSearchBoxMode()));

        return super.toTag(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBoolean(isActive());
        buf.writeInt(GridSettings.getSortingDirection(getSortingDirection()));
        buf.writeInt(GridSettings.getSortingType(getSortingType()));
        buf.writeInt(GridSettings.getSize(getSize()));
        buf.writeInt(RefinedStorage2Mod.API.getGridSearchBoxModeRegistry().getId(getSearchBoxMode()));

        Collection<ItemStack> stacks = getNetwork().getItemStorageChannel().getStacks();

        buf.writeInt(stacks.size());
        stacks.forEach(stack -> {
            buf.writeItemStack(stack);
            buf.writeInt(stack.getCount());
            PacketUtil.writeTrackerEntry(buf, getNetwork().getItemStorageChannel().getTracker().getEntry(stack));
        });
    }

    public GridSortingType getSortingType() {
        return node.getSortingType();
    }

    public GridSortingDirection getSortingDirection() {
        return node.getSortingDirection();
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return node.getSearchBoxMode();
    }

    public GridSize getSize() {
        return node.getSize();
    }

    public void setSortingType(GridSortingType sortingType) {
        node.setSortingType(sortingType);
        markDirty();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        node.setSortingDirection(sortingDirection);
        markDirty();
    }

    public void setSize(GridSize size) {
        node.setSize(size);
        markDirty();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        node.setSearchBoxMode(searchBoxMode);
        markDirty();
    }

    public void addWatcher(GridEventHandler eventHandler) {
        node.addWatcher(eventHandler);
    }

    public void removeWatcher(GridEventHandler eventHandler) {
        node.removeWatcher(eventHandler);
    }
}
