package com.refinedmods.refinedstorage2.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.core.network.node.grid.GridNetworkNode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.block.entity.NetworkNodeBlockEntity;
import com.refinedmods.refinedstorage2.fabric.coreimpl.network.node.FabricNetworkNodeReference;
import com.refinedmods.refinedstorage2.fabric.screen.handler.GridScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class GridBlockEntity extends NetworkNodeBlockEntity<GridNetworkNode> implements NamedScreenHandlerFactory {
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
        return new GridScreenHandler(syncId, playerInventory);
    }
}
