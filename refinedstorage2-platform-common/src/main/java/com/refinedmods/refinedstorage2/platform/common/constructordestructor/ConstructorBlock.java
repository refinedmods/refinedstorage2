package com.refinedmods.refinedstorage2.platform.common.constructordestructor;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage2.platform.common.support.NamedBlockItem;
import com.refinedmods.refinedstorage2.platform.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ConstructorBlock extends AbstractConstructorDestructorBlock<
    ConstructorBlock, ConstructorBlockEntity, NamedBlockItem
    > implements BlockItemProvider<NamedBlockItem> {
    private static final Component HELP = createTranslation("item", "constructor.help");

    public ConstructorBlock(final DyeColor color, final MutableComponent name) {
        super(color, name, new NetworkNodeBlockEntityTicker<>(
            BlockEntities.INSTANCE::getConstructor,
            ACTIVE
        ));
    }

    @Override
    public BlockColorMap<ConstructorBlock, NamedBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getConstructor();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new ConstructorBlockEntity(blockPos, blockState);
    }

    @Override
    public NamedBlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), getName(), HELP);
    }
}
