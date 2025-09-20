package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.node.GraphNetworkComponent;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.support.network.ConnectionSink;
import com.refinedmods.refinedstorage.common.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.BlockEntityWithDrops;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.network.ColoredConnectionStrategy;
import com.refinedmods.refinedstorage.common.util.ContainerUtil;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class NetworkTransmitterBlockEntity
    extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<NetworkTransmitterData>, BlockEntityWithDrops {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkTransmitterBlockEntity.class);

    private static final NetworkTransmitterData INACTIVE = NetworkTransmitterData.message(
        false,
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
                containers.update(level);
            }
        });
    }

    @Override
    protected InWorldNetworkNodeContainer createMainContainer(final SimpleNetworkNode networkNode) {
        return RefinedStorageApi.INSTANCE.createNetworkNodeContainer(this, networkNode)
            .connectionStrategy(new ColoredConnectionStrategy(this::getBlockState, getBlockPos()) {
                @Override
                public void addOutgoingConnections(final ConnectionSink sink) {
                    super.addOutgoingConnections(sink);
                    if (receiverKey != null && NetworkTransmitterBlockEntity.this.mainNetworkNode.isActive()) {
                        sink.tryConnect(receiverKey.pos(), NetworkReceiverBlock.class);
                    }
                }
            })
            .build();
    }

    @Override
    protected void activenessChanged(final boolean newActive) {
        super.activenessChanged(newActive);
        containers.update(level);
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
        if (!mainNetworkNode.isActive()) {
            return NetworkTransmitterState.INACTIVE;
        }
        if (receiverKey == null) {
            return NetworkTransmitterState.ERROR;
        }
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return NetworkTransmitterState.ERROR;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(network, receiverKey);
        return receiverFound ? NetworkTransmitterState.ACTIVE : NetworkTransmitterState.ERROR;
    }

    NetworkTransmitterData getStatus() {
        final Network network = mainNetworkNode.getNetwork();
        if (!mainNetworkNode.isActive() || network == null || level == null) {
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
        return NetworkTransmitterData.message(true, message);
    }

    @Override
    public void doWork() {
        super.doWork();
        if (!mainNetworkNode.isActive() || mainNetworkNode.getNetwork() == null || receiverKey == null) {
            return;
        }
        final boolean receiverFound = isReceiverFoundInNetwork(mainNetworkNode.getNetwork(), receiverKey);
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
        if (isReceiverFoundInLevel()) {
            containers.update(level);
        }
    }

    private boolean isReceiverFoundInLevel() {
        if (level == null || receiverKey == null) {
            return false;
        }
        final MinecraftServer server = level.getServer();
        if (server == null) {
            return false;
        }
        final Level level = server.getLevel(receiverKey.pos().dimension());
        if (level == null || !level.isLoaded(receiverKey.pos().pos())) {
            return false;
        }
        final BlockState blockState = level.getBlockState(receiverKey.pos().pos());
        return blockState.getBlock() instanceof NetworkReceiverBlock;
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
        tag.put(TAG_NETWORK_CARD_INVENTORY, ContainerUtil.write(networkCardInventory, provider));
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains(TAG_NETWORK_CARD_INVENTORY)) {
            ContainerUtil.read(tag.getCompound(TAG_NETWORK_CARD_INVENTORY), networkCardInventory, provider);
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
    public Component getName() {
        return overrideName(ContentNames.NETWORK_TRANSMITTER);
    }

    @Override
    public final NonNullList<ItemStack> getDrops() {
        return NonNullList.of(ItemStack.EMPTY, networkCardInventory.getNetworkCard());
    }
}
