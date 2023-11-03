package com.refinedmods.refinedstorage2.platform.common.support.network;

import com.refinedmods.refinedstorage2.api.network.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.support.PlayerAware;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.support.RedstoneModeSettings;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractRedstoneModeNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends NetworkNodeContainerBlockEntityImpl<T> implements PlayerAware, ConfigurationCardTarget {
    private static final String TAG_REDSTONE_MODE = "rm";
    private static final String TAG_PLACED_BY_PLAYER_ID = "pbpid";

    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    @Nullable
    private UUID placedByPlayerId;

    protected AbstractRedstoneModeNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                                                  final BlockPos pos,
                                                                  final BlockState state,
                                                                  final T node) {
        super(type, pos, state, node);
    }

    @Override
    protected boolean isActive() {
        return super.isActive() && level != null && redstoneMode.isActive(level.hasNeighborSignal(worldPosition));
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        writeConfiguration(tag);
        if (placedByPlayerId != null) {
            tag.putUUID(TAG_PLACED_BY_PLAYER_ID, placedByPlayerId);
        }
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        readConfiguration(tag);
        if (tag.hasUUID(TAG_PLACED_BY_PLAYER_ID)) {
            placedByPlayerId = tag.getUUID(TAG_PLACED_BY_PLAYER_ID);
        }
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        if (tag.contains(TAG_REDSTONE_MODE)) {
            redstoneMode = RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE));
        }
    }

    @Override
    public List<Item> getUpgradeItems() {
        return Collections.emptyList();
    }

    @Override
    public boolean addUpgradeItem(final Item upgradeItem) {
        return false;
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    @Override
    public void setPlacedBy(final UUID playerId) {
        this.placedByPlayerId = playerId;
        setChanged();
    }

    protected final Player getFakePlayer(final ServerLevel serverLevel) {
        return Platform.INSTANCE.getFakePlayer(serverLevel, placedByPlayerId);
    }
}
