package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class GridBlockEntity extends AbstractGridBlockEntity implements NetworkNodeExtendedMenuProvider<GridData> {
    public GridBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getGrid(), pos, state, Platform.INSTANCE.getConfig().getGrid().getEnergyUsage());
    }

    @Override
    public GridData getMenuData() {
        return GridData.of(this);
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, GridData> getMenuCodec() {
        return GridData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.GRID);
    }

    @Override
    @Nullable
    public AbstractGridContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new GridContainerMenu(syncId, inventory, this);
    }
}
