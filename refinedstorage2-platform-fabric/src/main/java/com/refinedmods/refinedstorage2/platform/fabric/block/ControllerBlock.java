package com.refinedmods.refinedstorage2.platform.fabric.block;

import com.refinedmods.refinedstorage2.api.network.node.controller.ControllerType;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.ticker.ControllerBlockEntityTicker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ControllerBlock extends NetworkNodeBlock {
    public static final EnumProperty<ControllerEnergyType> ENERGY_TYPE = EnumProperty.of("energy_type", ControllerEnergyType.class);

    private final ControllerType type;

    public ControllerBlock(Settings settings, ControllerType type) {
        super(settings);

        this.type = type;

        setDefaultState(getStateManager().getDefaultState().with(ENERGY_TYPE, ControllerEnergyType.OFF));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        ActionResult result = Rs2Mod.BLOCKS.getController().updateColor(state, player.getStackInHand(hand), world, pos, player);
        if (result != ActionResult.PASS) {
            return result;
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(ENERGY_TYPE);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerBlockEntity(type, pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return (BlockEntityTicker<T>) new ControllerBlockEntityTicker();
    }
}
