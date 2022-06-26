package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ControllerBlock extends NetworkNodeContainerBlock implements ColorableBlock<ControllerBlock> {
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
        return (type == BlockEntities.INSTANCE.getController() || type == BlockEntities.INSTANCE.getCreativeController()) && !level.isClientSide ? (level2, pos, state2, blockEntity) -> ControllerBlockEntity.serverTick(state2, (ControllerBlockEntity) blockEntity) : null;
    }

    @Override
    public BlockColorMap<ControllerBlock> getBlockColorMap() {
        return type == ControllerType.CREATIVE
                ? Blocks.INSTANCE.getCreativeController()
                : Blocks.INSTANCE.getController();
    }
}
