package com.refinedmods.refinedstorage.platform.common.support.network;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.platform.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.support.PlayerAwareBlockEntity;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.platform.common.support.RedstoneModeSettings;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractRedstoneModeNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends BaseNetworkNodeContainerBlockEntity<T> implements PlayerAwareBlockEntity, ConfigurationCardTarget {
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
    protected boolean calculateActive() {
        return super.calculateActive()
            && level != null
            && redstoneMode.isActive(level.hasNeighborSignal(worldPosition));
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        writeConfiguration(tag, provider);
        if (placedByPlayerId != null) {
            tag.putUUID(TAG_PLACED_BY_PLAYER_ID, placedByPlayerId);
        }
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(getRedstoneMode()));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        readConfiguration(tag, provider);
        if (tag.hasUUID(TAG_PLACED_BY_PLAYER_ID)) {
            placedByPlayerId = tag.getUUID(TAG_PLACED_BY_PLAYER_ID);
        }
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_REDSTONE_MODE)) {
            redstoneMode = RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE));
        }
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

    protected final boolean isPlacedBy(final UUID playerId) {
        return Objects.equals(placedByPlayerId, playerId);
    }
}
