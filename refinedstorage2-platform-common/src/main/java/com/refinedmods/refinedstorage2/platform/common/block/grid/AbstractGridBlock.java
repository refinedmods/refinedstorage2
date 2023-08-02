
package com.refinedmods.refinedstorage2.platform.common.block.grid;

import com.refinedmods.refinedstorage2.platform.common.block.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage2.platform.common.block.BlockConstants;
import com.refinedmods.refinedstorage2.platform.common.block.BlockItemProvider;
import com.refinedmods.refinedstorage2.platform.common.block.ColorableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.direction.BiDirectionType;
import com.refinedmods.refinedstorage2.platform.common.block.direction.DirectionType;
import com.refinedmods.refinedstorage2.platform.common.util.BiDirection;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class AbstractGridBlock<T extends AbstractGridBlock<T> & BlockItemProvider>
    extends AbstractDirectionalBlock<BiDirection>
    implements EntityBlock, ColorableBlock<T> {
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
