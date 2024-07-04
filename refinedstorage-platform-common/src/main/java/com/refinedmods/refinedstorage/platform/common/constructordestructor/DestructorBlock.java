package com.refinedmods.refinedstorage.platform.common.constructordestructor;

import com.refinedmods.refinedstorage.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.Blocks;
import com.refinedmods.refinedstorage.platform.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.platform.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.platform.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class DestructorBlock extends AbstractConstructorDestructorBlock<
    DestructorBlock, DestructorBlockEntity, BaseBlockItem
    > implements BlockItemProvider<BaseBlockItem> {
    private static final Component HELP = createTranslation("item", "destructor.help");

    public DestructorBlock(final DyeColor color, final MutableComponent name) {
        super(color, name, new NetworkNodeBlockEntityTicker<>(
            BlockEntities.INSTANCE::getDestructor,
            ACTIVE
        ));
    }

    @Override
    public BlockColorMap<DestructorBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getDestructor();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new DestructorBlockEntity(blockPos, blockState);
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(this, HELP);
    }
}
