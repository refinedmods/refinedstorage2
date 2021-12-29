package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class ControllerBlock extends NetworkNodeContainerBlock {
    public static final EnumProperty<ControllerEnergyType> ENERGY_TYPE = EnumProperty.create("energy_type", ControllerEnergyType.class);

    private final ControllerType type;
    private final MutableComponent name;

    public ControllerBlock(ControllerType type, MutableComponent name) {
        super(BlockConstants.STONE_PROPERTIES);

        this.type = type;
        this.name = name;

        registerDefaultState(getStateDefinition().any().setValue(ENERGY_TYPE, ControllerEnergyType.OFF));
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = Blocks.INSTANCE.getController().updateColor(state, player.getItemInHand(hand), level, pos, player);
        if (result != InteractionResult.PASS) {
            return result;
        }

        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(ENERGY_TYPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ControllerBlockEntity(type, pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (type == BlockEntities.INSTANCE.getController() || type == BlockEntities.INSTANCE.getCreativeController()) && !level.isClientSide ? (level2, pos, state2, blockEntity) -> ControllerBlockEntity.serverTick(level2, state2, (ControllerBlockEntity) blockEntity) : null;
    }
}
