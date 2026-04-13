package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.common.content.BlockColorMap;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.BlockProperties;
import com.refinedmods.refinedstorage.common.content.Blocks;
import com.refinedmods.refinedstorage.common.support.AbstractActiveColoredDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.AbstractBlockEntityTicker;
import com.refinedmods.refinedstorage.common.support.BaseBlockItem;
import com.refinedmods.refinedstorage.common.support.BlockItemProvider;
import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;
import com.refinedmods.refinedstorage.common.support.direction.DirectionType;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirection;
import com.refinedmods.refinedstorage.common.support.direction.OrientedDirectionType;
import com.refinedmods.refinedstorage.common.support.network.NetworkNodeBlockEntityTicker;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AutocrafterManagerBlock
    extends AbstractActiveColoredDirectionalBlock<OrientedDirection, AutocrafterManagerBlock, BaseBlockItem>
    implements BlockItemProvider<BaseBlockItem>, EntityBlock {
    private static final Component HELP = createTranslation("item", "autocrafter_manager.help");
    private static final AbstractBlockEntityTicker<AutocrafterManagerBlockEntity> TICKER =
        new NetworkNodeBlockEntityTicker<>(BlockEntities.INSTANCE::getAutocrafterManager, ACTIVE);

    private final Identifier id;

    public AutocrafterManagerBlock(final Identifier id, final DyeColor color, final MutableComponent name) {
        super(BlockProperties.stone(id), color, name);
        this.id = id;
    }

    @Override
    protected DirectionType<OrientedDirection> getDirectionType() {
        return OrientedDirectionType.INSTANCE;
    }

    @Override
    public BlockColorMap<AutocrafterManagerBlock, BaseBlockItem> getBlockColorMap() {
        return Blocks.INSTANCE.getAutocrafterManager();
    }

    @Override
    public boolean canAlwaysConnect() {
        return true;
    }

    @Override
    public BaseBlockItem createBlockItem() {
        return new NetworkNodeBlockItem(id, this, HELP);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(final BlockPos blockPos, final BlockState blockState) {
        return new AutocrafterManagerBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level,
                                                                  final BlockState state,
                                                                  final BlockEntityType<T> type) {
        return TICKER.get(level, type);
    }
}
