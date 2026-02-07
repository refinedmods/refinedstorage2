package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.EntityBlock;

public abstract class AbstractGridBlock<T extends AbstractGridBlock<T, I> & BlockItemProvider<I>, I extends BlockItem>
    extends AbstractActiveColoredDirectionalBlock<OrientedDirection, T, I> implements EntityBlock {
    protected AbstractGridBlock(final Identifier id, final DyeColor color, final MutableComponent name) {
        super(BlockProperties.stone(id), color, name);
    }

    @Override
    protected DirectionType<OrientedDirection> getDirectionType() {
        return OrientedDirectionType.INSTANCE;
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }
}
