package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.core.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screen.handler.grid.GridScreenHandler;
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

import java.util.Collection;

public class GridBlockEntity extends NetworkNodeBlockEntity<GridNetworkNode> implements ExtendedScreenHandlerFactory {
    public static final int SORTING_ASCENDING = 0;
    public static final int SORTING_DESCENDING = 1;

    public static final int SORTING_TYPE_QUANTITY = 0;
    public static final int SORTING_TYPE_NAME = 1;
    public static final int SORTING_TYPE_ID = 2;
    public static final int SORTING_TYPE_LAST_MODIFIED = 3;

    private int sortingDirection = SORTING_ASCENDING;
    private int sortingType = SORTING_TYPE_QUANTITY;

    public GridBlockEntity() {
        super(RefinedStorage2Mod.BLOCK_ENTITIES.getGrid());
    }

    @Override
    protected GridNetworkNode createNode(World world, BlockPos pos) {
        return new GridNetworkNode(pos, FabricNetworkNodeReference.of(world, pos));
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("block.refinedstorage2.grid");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new GridScreenHandler(syncId, playerInventory, this);
    }

    public void setSortingDirection(int sortingDirection) {
        if (sortingDirection != 0 && sortingDirection != 1) {
            throw new IllegalArgumentException("Invalid sorting direction: " + sortingDirection);
        }
        this.sortingDirection = sortingDirection;
    }

    public void setSortingType(int sortingType) {
        if (sortingType < 0 || sortingType > 3) {
            throw new IllegalArgumentException("Invalid sorting type: " + sortingType);
        }
        this.sortingType = sortingType;
    }

    @Override
    public void fromTag(BlockState blockState, CompoundTag tag) {
        if (tag.contains("sd")) {
            setSortingDirection(tag.getInt("sd"));
        }

        if (tag.contains("st")) {
            setSortingType(tag.getInt("st"));
        }

        super.fromTag(blockState, tag);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("sd", sortingDirection);
        tag.putInt("st", sortingType);
        return super.toTag(tag);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeInt(sortingDirection);
        buf.writeInt(sortingType);

        Collection<ItemStack> stacks = getNetwork().getItemStorageChannel().getStacks();

        buf.writeInt(stacks.size());
        stacks.forEach(stack -> {
            buf.writeItemStack(stack);
            buf.writeInt(stack.getCount());
            PacketUtil.writeTrackerEntry(buf, getNetwork().getItemStorageChannel().getTracker().getEntry(stack));
        });
    }
}
