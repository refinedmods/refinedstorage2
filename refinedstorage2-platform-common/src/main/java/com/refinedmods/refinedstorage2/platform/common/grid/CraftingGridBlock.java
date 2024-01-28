package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.platform.common.content.BlockColorMap;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage2.platform.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage2.platform.common.support.NamedBlockItem;
import com.refinedmods.refinedstorage2.platform.common.support.network.NetworkNodeBlockEntityTicker;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class CraftingGridBlock extends AbstractGridBlock<CraftingGridBlock, NamedBlockItem>
    implements BlockItemProvider<NamedBlockItem> {
    private static final Component HELP = createTranslation("item", "crafting_grid.help");
    private static final AbstractBlockEntityTicker<CraftingGridBlockEntity> TICKER = new NetworkNodeBlockEntityTicker<>(
        BlockEntities.INSTANCE::getCraftingGrid,
        ACTIVE
    );

    public CraftingGridBlock(final DyeColor color, final MutableComponent name) {
        super(name, color);
    }

    @Override
    public BlockColorMap<CraftingGridBlock, NamedBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getCraftingGrid();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
        return new CraftingGridBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <O extends BlockEntity> BlockEntityTicker<O> getTicker(final Level level,
                                                                  final BlockState blockState,
                                                                  final BlockEntityType<O> type) {
        return TICKER.get(level, type);
    }

    @Override
    public NamedBlockItem createBlockItem() {
        return new NamedBlockItem(this, new Item.Properties(), getName(), HELP);
    }
}
