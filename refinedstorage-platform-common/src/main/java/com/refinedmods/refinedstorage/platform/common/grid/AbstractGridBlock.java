package com.refinedmods.refinedstorage.platform.common.grid;

import com.refinedmods.refinedstorage.platform.common.content.BlockConstants;
import com.refinedmods.refinedstorage.platform.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirection;
import com.refinedmods.refinedstorage.platform.common.support.direction.BiDirectionType;
import com.refinedmods.refinedstorage.platform.common.support.direction.DirectionType;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.EntityBlock;

public abstract class AbstractGridBlock<T extends AbstractGridBlock<T, I> & BlockItemProvider<I>, I extends BlockItem>
    extends AbstractActiveColoredDirectionalBlock<BiDirection, T, I> implements EntityBlock {
    protected AbstractGridBlock(final MutableComponent name, final DyeColor color) {
        super(BlockConstants.PROPERTIES, color, name);
    }

    @Override
    protected DirectionType<BiDirection> getDirectionType() {
        return BiDirectionType.INSTANCE;
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }
}
