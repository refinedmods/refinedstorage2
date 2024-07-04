package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.platform.common.Platform;
import com.refinedmods.refinedstorage.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage.platform.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class NetworkTransmitterBlockEntity
    extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<NetworkTransmitterData>, BlockEntityWithDrops {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkTransmitterBlockEntity.class);

    private static final NetworkTransmitterData INACTIVE = NetworkTransmitterData.message(
        createTranslation("gui", "network_transmitter.status.inactive")
    );
    private static final NetworkTransmitterData MISSING_NETWORK_CARD = NetworkTransmitterData.error(
        createTranslation("gui", "network_transmitter.status.missing_network_card").withStyle(ChatFormatting.DARK_RED)
    );
    private static final NetworkTransmitterData RECEIVER_UNREACHABLE = NetworkTransmitterData.error(
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
            updateReceiverLocation();
            if (level != null) {
                LOGGER.debug("Network card was changed at {}, sending network update", worldPosition);
                setChanged();
                updateContainers();
            }
        });
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        updateContainers();
    }

    public void updateStateInLevel(final BlockState state) {
        final NetworkTransmitterState currentState = state.getValue(NetworkTransmitterBlock.STATE);
        final NetworkTransmitterState newState = calculateState();
        if (currentState != newState && level != null && stateChangeRateLimiter.tryAcquire()) {
            LOGGER.debug("Updating network transmitter at {} from {} to {}", worldPosition, currentState, newState);
            level.setBlockAndUpdate(worldPosition, state.setValue(NetworkTransmitterBlock.STATE, newState));
        }
    }

    private NetworkTransmitterState calculateState() {
        if (!mainNode.isActive()) {
            return NetworkTransmitterState.INACTIVE;
        }
        if (receiverKey == null) {
            return NetworkTransmitterState.ERROR;
        }
        final Network network = mainNode.getNetwork();
        if (network == null) {
            return NetworkTransmitterState.ERROR;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(network, receiverKey);
        return receiverFound ? NetworkTransmitterState.ACTIVE : NetworkTransmitterState.ERROR;
    }

    NetworkTransmitterData getStatus() {
        final Network network = mainNode.getNetwork();
        if (!mainNode.isActive() || network == null || level == null) {
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
        return NetworkTransmitterData.message(message);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (!mainNode.isActive() || mainNode.getNetwork() == null || receiverKey == null) {
            return;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(mainNode.getNetwork(), receiverKey);
        if (!receiverFound && networkRebuildRetryRateLimiter.tryAcquire()) {
            tryReconnectingWithReceiver();
        }
    }

    private void tryReconnectingWithReceiver() {
        LOGGER.debug(
            "Receiver {} was not found in network for transmitter at {}, retrying and sending network update",
            receiverKey,
            worldPosition
        );
        updateContainers();
    }

    private static boolean isReceiverFoundInNetwork(final Network network, final NetworkReceiverKey key) {
        return network.getComponent(GraphNetworkComponent.class).getContainer(key) != null;
    }

    Container getNetworkCardInventory() {
        return networkCardInventory;
    }

    @Override
    public void saveAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put(TAG_NETWORK_CARD_INVENTORY, networkCardInventory.createTag(provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_NETWORK_CARD_INVENTORY)) {
            networkCardInventory.fromTag(
                tag.getList(TAG_NETWORK_CARD_INVENTORY, Tag.TAG_COMPOUND),
                provider
            );
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
    public NetworkTransmitterData getMenuData() {
        return getStatus();
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, NetworkTransmitterData> getMenuCodec() {
        return NetworkTransmitterData.STREAM_CODEC;
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
        if (receiverKey != null && mainNode.isActive()) {
            sink.tryConnect(receiverKey.pos());
        }
    }
}
