package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage2.platform.common.support.ColorableBlock;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage2.platform.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.support.direction.DirectionType;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class AbstractGridBlock<T extends AbstractGridBlock<T, I> & BlockItemProvider<I>, I extends BlockItem>
    extends AbstractDirectionalBlock<BiDirection>
    implements EntityBlock, ColorableBlock<T, I> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final MutableComponent name;
    private final DyeColor color;

    protected AbstractGridBlock(final MutableComponent name, final DyeColor color) {
        super(BlockConstants.PROPERTIES);
        this.name = name;
        this.color = color;
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    protected BlockState getDefaultState() {
        return super.getDefaultState().setValue(ACTIVE, false);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public MutableComponent getName() {
        return name;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }
}
