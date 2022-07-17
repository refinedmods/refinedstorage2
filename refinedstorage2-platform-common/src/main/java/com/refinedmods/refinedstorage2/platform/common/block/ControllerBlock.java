package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.ControllerBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ControllerBlock extends AbstractBaseBlock implements ColorableBlock<ControllerBlock>, EntityBlock {
    public static final EnumProperty<ControllerEnergyType> ENERGY_TYPE = EnumProperty.create(
        "energy_type",
        ControllerEnergyType.class
    );

    private final ControllerType type;
    private final MutableComponent name;
    private final ControllerBlockEntityTicker ticker;

    public ControllerBlock(final ControllerType type,
                           final MutableComponent name,
                           final ControllerBlockEntityTicker ticker) {
        super(BlockConstants.PROPERTIES);
        this.type = type;
        this.name = name;
        this.ticker = ticker;
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(ENERGY_TYPE, ControllerEnergyType.OFF);
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(ENERGY_TYPE);
    }

    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new ControllerBlockEntity(type, pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> blockEntityType) {
        return ticker.get(level, blockEntityType);
    }

    @Override
    public BlockColorMap<ControllerBlock> getBlockColorMap() {
        return type == ControllerType.CREATIVE
            ? Blocks.INSTANCE.getCreativeController()
            : Blocks.INSTANCE.getController();
    }
}
