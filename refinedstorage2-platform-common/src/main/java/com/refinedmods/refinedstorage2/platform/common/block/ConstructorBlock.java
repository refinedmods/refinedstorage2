package com.refinedmods.refinedstorage2.platform.common.block;

import com.refinedmods.refinedstorage2.platform.common.block.entity.constructor.ConstructorBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.ticker.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.item.block.NamedBlockItem;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ConstructorBlock extends AbstractConstructorDestructorBlock<ConstructorBlock, ConstructorBlockEntity>
    implements BlockItemProvider {
    private static final Component HELP = createTranslation("item", "constructor.help");

    public ConstructorBlock(final DyeColor color, final MutableComponent name) {
        super(color, name, new NetworkNodeBlockEntityTicker<>(
            BlockEntities.INSTANCE::getConstructor,
            ACTIVE
        ));
    }

    @Override
    public BlockColorMap<ConstructorBlock> getBlockColorMap() {
        return Blocks.INSTANCE.getConstructor();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new ConstructorBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), getName(), HELP);
    }
}
