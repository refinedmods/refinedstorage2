package com.refinedmods.refinedstorage.common.constructordestructor;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockEntityProvider;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;
import com.refinedmods.refinedstorage.common.util.IdentifierUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class DestructorBlock extends AbstractConstructorDestructorBlock<
    DestructorBlock, AbstractDestructorBlockEntity, BaseBlockItem
    > implements BlockItemProvider<BaseBlockItem> {
    private static final Component HELP = IdentifierUtil.createTranslation("item", "destructor.help");

    private final Identifier id;
    private final BlockEntityProvider<AbstractDestructorBlockEntity> blockEntityProvider;

    public DestructorBlock(final Identifier id,
                           final DyeColor color,
                           final MutableComponent name,
                           final BlockEntityProvider<AbstractDestructorBlockEntity> blockEntityProvider) {
        super(id, color, name, new NetworkNodeBlockEntityTicker<>(
            BlockEntities.INSTANCE::getDestructor,
            ACTIVE
        ));
        this.id = id;
        this.blockEntityProvider = blockEntityProvider;
    }

    @Override
    public BlockColorMap<DestructorBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getDestructor();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return blockEntityProvider.create(pos, state);
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }
}
