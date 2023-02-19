package com.refinedmods.refinedstorage2.platform.common.block.entity.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class GridBlockEntity extends AbstractGridBlockEntity {
    public GridBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getGrid(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return createTranslation("block", "grid");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new GridContainerMenu(syncId, inventory, this);
    }
}
