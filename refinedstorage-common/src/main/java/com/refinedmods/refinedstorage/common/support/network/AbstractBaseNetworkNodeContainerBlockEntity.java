package com.refinedmods.refinedstorage.common.support.network;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.energy.EnergyNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNode;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.configurationcard.ConfigurationCardTarget;
import com.refinedmods.refinedstorage.common.api.support.network.AbstractNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.api.support.network.item.NetworkItemTargetBlockEntity;
import com.refinedmods.refinedstorage.common.support.PlayerAwareBlockEntity;
import com.refinedmods.refinedstorage.common.support.RedstoneMode;
import com.refinedmods.refinedstorage.common.support.RedstoneModeSettings;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock.tryExtractDirection;

public abstract class AbstractBaseNetworkNodeContainerBlockEntity<T extends AbstractNetworkNode>
    extends AbstractNetworkNodeContainerBlockEntity<T>
    implements NetworkItemTargetBlockEntity, ConfigurationCardTarget, PlayerAwareBlockEntity, Nameable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseNetworkNodeContainerBlockEntity.class);
    private static final String TAG_CUSTOM_NAME = "CustomName";
    private static final String TAG_PLACED_BY_PLAYER_ID = "pbpid";
    private static final String TAG_REDSTONE_MODE = "rm";
    private static final String TAG_DEBUG_NETWORK_ID = "dnid";
    private static final int ACTIVENESS_CHANGE_TICK_RATE = 20;

    protected NetworkNodeTicker ticker = NetworkNodeTicker.IMMEDIATE;

    private int activenessChangeTicks;

    @Nullable
    private Component customName;
    @Nullable
    private UUID placedByPlayerId;
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    private int debugNetworkId = -1;

    protected AbstractBaseNetworkNodeContainerBlockEntity(final BlockEntityType<?> type,
                                                          final BlockPos pos,
                                                          final BlockState state,
                                                          final T networkNode) {
        super(type, pos, state, networkNode);
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final T networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .connectionStrategy(new ColoredConnectionStrategy(this::getBlockState, getBlockPos()))
            .build();
    }

    protected boolean calculateActive() {
        final long energyUsage = mainNetworkNode.getEnergyUsage();
        final boolean hasLevel = level != null && level.isLoaded(worldPosition);
        final boolean redstoneModeActive = !hasRedstoneMode()
            || this.redstoneMode == RedstoneMode.IGNORE
            || redstoneMode.isActive(hasLevel && level.hasNeighborSignal(worldPosition));
        final boolean hasEnergy = !RefinedStorageApi.INSTANCE.isEnergyRequired()
            || (mainNetworkNode.getNetwork() != null
            && mainNetworkNode.getNetwork().getComponent(EnergyNetworkComponent.class).getStored() >= energyUsage);
        return hasLevel && redstoneModeActive && hasEnergy;
    }

    public void updateActiveness(final BlockState state, @Nullable final BooleanProperty activenessProperty) {
        final boolean newActive = calculateActive();
        final boolean nodeActivenessNeedsUpdate = newActive != mainNetworkNode.isActive();
        final boolean blockStateActivenessNeedsUpdate = activenessProperty != null
            && state.getValue(activenessProperty) != newActive;
        final boolean activenessNeedsUpdate = nodeActivenessNeedsUpdate || blockStateActivenessNeedsUpdate;
        if (activenessNeedsUpdate && activenessChangeTicks++ % ACTIVENESS_CHANGE_TICK_RATE == 0) {
            if (nodeActivenessNeedsUpdate) {
                activenessChanged(newActive);
            }
            if (blockStateActivenessNeedsUpdate) {
                updateActivenessBlockState(state, activenessProperty, newActive);
            }
        }
    }

    protected void activenessChanged(final boolean newActive) {
        LOGGER.debug(
            "Activeness change for node at {}: {} -> {}",
            getBlockPos(),
            mainNetworkNode.isActive(),
            newActive
        );
        mainNetworkNode.setActive(newActive);
    }

    private void updateActivenessBlockState(final BlockState state,
                                            final BooleanProperty activenessProperty,
                                            final boolean active) {
        if (level != null) {
            LOGGER.debug(
                "Sending block update at {} due to activeness change: {} -> {}",
                getBlockPos(),
                state.getValue(activenessProperty),
                active
            );
            level.setBlockAndUpdate(getBlockPos(), state.setValue(activenessProperty, active));
        }
    }

    public void doWork() {
        ticker.tick(mainNetworkNode);
    }

    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(
        final BlockState oldBlockState,
        final BlockState newBlockState
    ) {
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(final BlockState newBlockState) {
        final BlockState oldBlockState = getBlockState();
        super.setBlockState(newBlockState);
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
        if (doesBlockStateChangeWarrantNetworkNodeUpdate(oldBlockState, newBlockState)) {
            containers.update(level);
        }
    }

    @Override
    public void setLevel(final Level level) {
        super.setLevel(level);
        if (level instanceof ServerLevel serverLevel) {
            initialize(serverLevel);
        }
    }

    protected final void initialize(final ServerLevel level) {
        final Direction direction = tryExtractDirection(getBlockState());
        if (direction == null) {
            return;
        }
        initialize(level, direction);
    }

    protected void initialize(final ServerLevel level, final Direction direction) {
        // no op
    }

    @Nullable
    @Override
    public Network getNetworkForItem() {
        return mainNetworkNode.getNetwork();
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (placedByPlayerId != null) {
            tag.putUUID(TAG_PLACED_BY_PLAYER_ID, placedByPlayerId);
        }
        writeConfiguration(tag, provider);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID(TAG_PLACED_BY_PLAYER_ID)) {
            setPlacedBy(tag.getUUID(TAG_PLACED_BY_PLAYER_ID));
        }
        if (tag.contains(TAG_DEBUG_NETWORK_ID)) {
            debugNetworkId = tag.getInt(TAG_DEBUG_NETWORK_ID);
            Platform.INSTANCE.requestModelDataUpdateOnClient(this, true);
        }
        readConfiguration(tag, provider);
    }

    public int getDebugNetworkId() {
        return debugNetworkId;
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (customName != null) {
            tag.putString(TAG_CUSTOM_NAME, Component.Serializer.toJson(customName, provider));
        }
        if (hasRedstoneMode()) {
            tag.putInt(TAG_REDSTONE_MODE, RedstoneModeSettings.getRedstoneMode(redstoneMode));
        }
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_CUSTOM_NAME, Tag.TAG_STRING)) {
            this.customName = parseCustomNameSafe(tag.getString(TAG_CUSTOM_NAME), provider);
        }
        if (hasRedstoneMode() && tag.contains(TAG_REDSTONE_MODE)) {
            this.redstoneMode = RedstoneModeSettings.getRedstoneMode(tag.getInt(TAG_REDSTONE_MODE));
        }
    }

    protected boolean hasRedstoneMode() {
        return true;
    }

    private void verifyHasRedstoneMode() {
        if (!hasRedstoneMode()) {
            throw new IllegalStateException("Block has no redstone mode!");
        }
    }

    public RedstoneMode getRedstoneMode() {
        verifyHasRedstoneMode();
        return redstoneMode;
    }

    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        verifyHasRedstoneMode();
        this.redstoneMode = redstoneMode;
        setChanged();
    }

    @Override
    protected void applyImplicitComponents(final BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        this.customName = componentInput.get(DataComponents.CUSTOM_NAME);
    }

    @Override
    protected void collectImplicitComponents(final DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.CUSTOM_NAME, customName);
    }

    protected final void setCustomName(@Nullable final Component customName) {
        this.customName = customName;
    }

    @Nullable
    @Override
    public final Component getCustomName() {
        return customName;
    }

    protected final Component overrideName(final Component defaultName) {
        return customName == null ? defaultName : customName;
    }

    @Override
    public final Component getDisplayName() {
        return getName();
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

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        if (!Platform.INSTANCE.getConfig().isDebug()) {
            return super.getUpdatePacket();
        }
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        if (!Platform.INSTANCE.getConfig().isDebug()) {
            return super.getUpdateTag(provider);
        }
        final CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt(TAG_DEBUG_NETWORK_ID, debugNetworkId);
        return tag;
    }

    public void updateDebugNetworkId() {
        final int newNetworkId = mainNetworkNode.getNetwork() == null ? -1 : mainNetworkNode.getNetwork().hashCode();
        if (debugNetworkId != newNetworkId) {
            debugNetworkId = newNetworkId;
            PlatformUtil.sendBlockUpdateToClient(level, getBlockPos());
        }
    }
}
