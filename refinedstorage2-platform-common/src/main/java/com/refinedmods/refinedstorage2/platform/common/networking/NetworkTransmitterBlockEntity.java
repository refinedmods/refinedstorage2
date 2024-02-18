package com.refinedmods.refinedstorage2.platform.common.networking;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class NetworkTransmitterBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider, BlockEntityWithDrops {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkTransmitterBlockEntity.class);

    private static final NetworkTransmitterStatus INACTIVE = NetworkTransmitterStatus.message(
        createTranslation("gui", "network_transmitter.status.inactive")
    );
    private static final NetworkTransmitterStatus MISSING_NETWORK_CARD = NetworkTransmitterStatus.error(
        createTranslation("gui", "network_transmitter.status.missing_network_card").withStyle(ChatFormatting.DARK_RED)
    );
    private static final NetworkTransmitterStatus RECEIVER_UNREACHABLE = NetworkTransmitterStatus.error(
        createTranslation("gui", "network_transmitter.status.receiver_unreachable").withStyle(ChatFormatting.DARK_RED)
    );

    private static final String TAG_NETWORK_CARD_INVENTORY = "nc";

    private final NetworkCardInventory networkCardInventory = new NetworkCardInventory();
    private final RateLimiter stateChangeRateLimiter = RateLimiter.create(1);
    private final RateLimiter networkRebuildRetryRateLimiter = RateLimiter.create(1 / 5D);

    @Nullable
    private NetworkReceiverKey receiverKey;

    public NetworkTransmitterBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getNetworkTransmitter(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getNetworkTransmitter().getEnergyUsage()
        ));
        networkCardInventory.addListener(container -> {
            setChanged();
            updateReceiverLocation();
            if (level != null) {
                LOGGER.info("Network card was changed at {}, sending network update", worldPosition);
                PlatformApi.INSTANCE.requestNetworkNodeUpdate(this, level);
            }
        });
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        if (level == null) {
            return;
        }
        PlatformApi.INSTANCE.requestNetworkNodeUpdate(this, level);
    }

    public void updateStateInLevel(final BlockState state) {
        final NetworkTransmitterState currentState = state.getValue(NetworkTransmitterBlock.STATE);
        final NetworkTransmitterState newState = getState();
        if (currentState != newState && level != null && stateChangeRateLimiter.tryAcquire()) {
            LOGGER.info("Updating network transmitter at {} from {} to {}", worldPosition, currentState, newState);
            level.setBlockAndUpdate(worldPosition, state.setValue(NetworkTransmitterBlock.STATE, newState));
        }
    }

    private NetworkTransmitterState getState() {
        if (!isActive()) {
            return NetworkTransmitterState.INACTIVE;
        }
        if (receiverKey == null) {
            return NetworkTransmitterState.ERROR;
        }
        final Network network = getNode().getNetwork();
        if (network == null) {
            return NetworkTransmitterState.ERROR;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(network, receiverKey);
        return receiverFound ? NetworkTransmitterState.ACTIVE : NetworkTransmitterState.ERROR;
    }

    NetworkTransmitterStatus getStatus() {
        final Network network = getNode().getNetwork();
        if (!isActive() || network == null || level == null) {
            return INACTIVE;
        }
        if (receiverKey == null) {
            return MISSING_NETWORK_CARD;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(network, receiverKey);
        if (!receiverFound) {
            return RECEIVER_UNREACHABLE;
        }
        final boolean showDistance = level.dimension() == receiverKey.pos().dimension();
        final MutableComponent message = showDistance ? createTranslation(
            "gui",
            "network_transmitter.status.transmitting",
            receiverKey.getDistance(worldPosition)) : receiverKey.getDimensionName();
        return NetworkTransmitterStatus.message(message);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (!isActive() || getNode().getNetwork() == null || receiverKey == null) {
            return;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(getNode().getNetwork(), receiverKey);
        if (!receiverFound && networkRebuildRetryRateLimiter.tryAcquire()) {
            tryReconnectingWithReceiver();
        }
    }

    private void tryReconnectingWithReceiver() {
        if (level == null) {
            return;
        }
        LOGGER.info(
            "Receiver {} was not found in network for transmitter at {}, retrying and sending network update",
            receiverKey,
            worldPosition
        );
        PlatformApi.INSTANCE.requestNetworkNodeUpdate(this, level);
    }

    private static boolean isReceiverFoundInNetwork(final Network network, final NetworkReceiverKey key) {
        return network.getComponent(GraphNetworkComponent.class).getContainer(key) != null;
    }

    Container getNetworkCardInventory() {
        return networkCardInventory;
    }

    @Override
    public void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_NETWORK_CARD_INVENTORY, networkCardInventory.createTag());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_NETWORK_CARD_INVENTORY)) {
            networkCardInventory.fromTag(tag.getList(TAG_NETWORK_CARD_INVENTORY, Tag.TAG_COMPOUND));
        }
        updateReceiverLocation();
    }

    private void updateReceiverLocation() {
        receiverKey = networkCardInventory.getReceiverLocation().map(NetworkReceiverKey::new).orElse(null);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new NetworkTransmitterContainerMenu(syncId, inventory, this);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        final NetworkTransmitterStatus status = getStatus();
        buf.writeBoolean(status.error());
        buf.writeComponent(status.message());
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.NETWORK_TRANSMITTER;
    }

    @Override
    public NonNullList<ItemStack> getDrops() {
        return NonNullList.of(ItemStack.EMPTY, networkCardInventory.getNetworkCard());
    }

    @Override
    public void addOutgoingConnections(final ConnectionSink sink) {
        super.addOutgoingConnections(sink);
        if (receiverKey != null && isActive()) {
            sink.tryConnect(receiverKey.pos());
        }
    }
}
