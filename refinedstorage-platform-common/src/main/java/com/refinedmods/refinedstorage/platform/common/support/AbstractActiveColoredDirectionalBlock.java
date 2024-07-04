package com.refinedmods.refinedstorage.platform.common.support;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class AbstractActiveColoredDirectionalBlock<T extends Enum<T> & StringRepresentable,
    B extends Block & BlockItemProvider<I>, I extends BlockItem>
    extends AbstractDirectionalBlock<T>
    implements ColorableBlock<B, I> {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final DyeColor color;
    private final MutableComponent name;

    protected AbstractActiveColoredDirectionalBlock(final Properties properties,
                                                    final DyeColor color,
                                                    final MutableComponent name) {
        super(properties);
        this.color = color;
        this.name = name;
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
    public DyeColor getColor() {
        return color;
    }

    @Override
    public MutableComponent getName() {
        return name;
    }
}
