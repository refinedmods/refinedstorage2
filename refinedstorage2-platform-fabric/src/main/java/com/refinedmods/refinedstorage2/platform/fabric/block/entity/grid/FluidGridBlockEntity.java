package com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid;

import com.refinedmods.refinedstorage2.api.stack.fluid.Rs2FluidStack;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.FluidGridScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class FluidGridBlockEntity extends GridBlockEntity<Rs2FluidStack> {
    public FluidGridBlockEntity(BlockPos pos, BlockState state) {
        super(Rs2Mod.BLOCK_ENTITIES.getFluidGrid(), pos, state, StorageChannelTypes.FLUID);
    }

    @Override
    public Text getDisplayName() {
        return Rs2Mod.createTranslation("block", "fluid_grid");
    }

    @Override
    protected void writeStack(PacketByteBuf buf, Rs2FluidStack stack) {
        PacketUtil.writeFluidStack(buf, stack, true);
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new FluidGridScreenHandler(syncId, inv, this);
    }
}
