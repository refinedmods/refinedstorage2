package com.refinedmods.refinedstorage.common.storagemonitor;

import com.refinedmods.refinedstorage.api.autocrafting.preview.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.preview.PreviewProvider;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.autocrafting.AutocraftingNetworkComponent;
import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage.api.network.storage.StorageNetworkComponent;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.root.RootStorage;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.storage.root.FuzzyRootStorage;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage.common.content.BlockEntities;
import com.refinedmods.refinedstorage.common.content.ContentNames;
import com.refinedmods.refinedstorage.common.support.AbstractDirectionalBlock;
import com.refinedmods.refinedstorage.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage.common.support.containermenu.NetworkNodeExtendedMenuProvider;
import com.refinedmods.refinedstorage.common.support.network.AbstractBaseNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.common.support.resource.ItemResource;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerData;
import com.refinedmods.refinedstorage.common.support.resource.ResourceContainerImpl;
import com.refinedmods.refinedstorage.common.util.PlatformUtil;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageMonitorBlockEntity extends AbstractBaseNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements NetworkNodeExtendedMenuProvider<ResourceContainerData>, PreviewProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageMonitorBlockEntity.class);

    private static final String TAG_CLIENT_FILTER = "cf";
    private static final String TAG_CLIENT_AMOUNT = "ca";
    private static final String TAG_CLIENT_ACTIVE = "cac";

    private final FilterWithFuzzyMode filter;
    private final RateLimiter displayUpdateRateLimiter = RateLimiter.create(0.25);
    private final StorageMonitorInsertTracker insertTracker = new StorageMonitorInsertTracker();

    private long currentAmount;
    private boolean currentlyActive;
    private long lastExtractTime;

    public StorageMonitorBlockEntity(final BlockPos pos, final BlockState state) {
        super(BlockEntities.INSTANCE.getStorageMonitor(), pos, state, new SimpleNetworkNode(
            Platform.INSTANCE.getConfig().getStorageMonitor().getEnergyUsage()
        ));
        final ResourceContainer resourceContainer = ResourceContainerImpl.createForFilter(1);
        this.filter = FilterWithFuzzyMode.create(resourceContainer, () -> {
            setChanged();
            sendDisplayUpdate();
        });
    }

    @Override
    public void doWork() {
        super.doWork();
        if (level == null) {
            return;
        }
        trySendDisplayUpdate(level);
    }

    private void trySendDisplayUpdate(final Level level) {
        final long amount = getAmount();
        final boolean active = mainNetworkNode.isActive();
        if ((amount != currentAmount || active != currentlyActive) && displayUpdateRateLimiter.tryAcquire()) {
            sendDisplayUpdate(level, amount, active);
        }
    }

    private long getAmount() {
        final ResourceKey configuredResource = getConfiguredResource();
        if (configuredResource == null) {
            return 0;
        }
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return 0;
        }
        return getAmount(network, configuredResource);
    }

    private long getAmount(final Network network, final ResourceKey configuredResource) {
        final RootStorage rootStorage = network.getComponent(StorageNetworkComponent.class);
        if (!filter.isFuzzyMode() || !(rootStorage instanceof FuzzyRootStorage fuzzyRootStorage)) {
            return rootStorage.get(configuredResource);
        }
        return fuzzyRootStorage.getFuzzy(configuredResource)
            .stream()
            .mapToLong(rootStorage::get)
            .sum();
    }

    public void extract(final ServerPlayer player) {
        if (level == null) {
            return;
        }
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return;
        }
        final PlatformResourceKey configuredResource = getConfiguredResource();
        if (configuredResource == null) {
            return;
        }
        final boolean extracted = doExtract(level, player, configuredResource, network);
        if (extracted) {
            lastExtractTime = System.currentTimeMillis();
        }
        if (!extracted && System.currentTimeMillis() - lastExtractTime > 250) {
            tryAutocrafting(player, network, configuredResource);
        }
    }

    private void tryAutocrafting(final ServerPlayer player,
                                 final Network network,
                                 final PlatformResourceKey configuredResource) {
        final boolean autocraftable = network.getComponent(AutocraftingNetworkComponent.class)
            .getOutputs()
            .contains(configuredResource);
        if (autocraftable) {
            Platform.INSTANCE.getMenuOpener().openMenu(
                player,
                new AutocraftingStorageMonitorExtendedMenuProvider(configuredResource, this)
            );
        }
    }

    private boolean doExtract(
        final Level level,
        final Player player,
        final ResourceKey configuredResource,
        final Network network
    ) {
        final boolean success = RefinedStorageApi.INSTANCE.getStorageMonitorExtractionStrategy().extract(
            configuredResource,
            !player.isShiftKeyDown(),
            player,
            new PlayerActor(player),
            network
        );
        if (!success) {
            return false;
        }
        sendDisplayUpdate();
        level.playSound(
            null,
            getBlockPos(),
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            .2f,
            ((level.random.nextFloat() - level.random.nextFloat()) * .7f + 1) * 2
        );
        return true;
    }

    public void insert(final Player player, final InteractionHand hand) {
        if (level != null && doInsert(player, hand)) {
            sendDisplayUpdate();
        }
    }

    private boolean doInsert(final Player player, final InteractionHand hand) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return false;
        }
        final ResourceKey configuredResource = getConfiguredResource();
        if (configuredResource == null) {
            return false;
        }
        final ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return doInsertAll(player);
        }
        return doInsert(player, hand, heldStack, configuredResource, network);
    }

    private boolean doInsert(
        final Player player,
        final InteractionHand hand,
        final ItemStack heldStack,
        final ResourceKey configuredResource,
        final Network network
    ) {
        return RefinedStorageApi.INSTANCE.getStorageMonitorInsertionStrategy().insert(
            configuredResource,
            heldStack,
            new PlayerActor(player),
            network
        ).map(result -> {
            insertTracker.trackInsertedItem(player.getGameProfile(), heldStack);
            player.setItemInHand(hand, result);
            return true;
        }).orElse(false);
    }

    private boolean doInsertAll(final Player player) {
        return insertTracker.getLastInsertedItem(player.getGameProfile()).map(
            item -> doInsertAll(player, item)
        ).orElse(false);
    }

    private boolean doInsertAll(final Player player, final ItemResource lastInsertedItem) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return false;
        }
        final ResourceKey configuredResource = getConfiguredResource();
        if (configuredResource == null) {
            return false;
        }
        boolean success = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            success |= tryInsertSlot(player, lastInsertedItem, i, configuredResource, network);
        }
        return success;
    }

    private boolean tryInsertSlot(
        final Player player,
        final ItemResource lastInsertedItem,
        final int inventorySlotIndex,
        final ResourceKey configuredResource,
        final Network network
    ) {
        final ItemStack slot = player.getInventory().getItem(inventorySlotIndex);
        if (slot.isEmpty()) {
            return false;
        }
        final ItemResource itemInSlot = ItemResource.ofItemStack(slot);
        if (!itemInSlot.equals(lastInsertedItem)) {
            return false;
        }
        return RefinedStorageApi.INSTANCE.getStorageMonitorInsertionStrategy().insert(
            configuredResource,
            slot,
            new PlayerActor(player),
            network
        ).map(result -> {
            player.getInventory().setItem(inventorySlotIndex, result);
            return true;
        }).orElse(false);
    }

    public boolean isFuzzyMode() {
        return filter.isFuzzyMode();
    }

    public void setFuzzyMode(final boolean fuzzyMode) {
        filter.setFuzzyMode(fuzzyMode);
    }

    @Nullable
    public PlatformResourceKey getConfiguredResource() {
        return filter.getFilterContainer().getResource(0);
    }

    public long getCurrentAmount() {
        return currentAmount;
    }

    public boolean isCurrentlyActive() {
        return currentlyActive;
    }

    @Override
    public void writeConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.writeConfiguration(tag, provider);
        filter.save(tag, provider);
    }

    @Override
    public void loadAdditional(final CompoundTag tag, final HolderLookup.Provider provider) {
        if (tag.contains(TAG_CLIENT_FILTER) && tag.contains(TAG_CLIENT_AMOUNT) && tag.contains(TAG_CLIENT_ACTIVE)) {
            filter.getFilterContainer().fromTag(tag.getCompound(TAG_CLIENT_FILTER), provider);
            currentAmount = tag.getLong(TAG_CLIENT_AMOUNT);
            currentlyActive = tag.getBoolean(TAG_CLIENT_ACTIVE);
        }
        super.loadAdditional(tag, provider);
    }

    @Override
    public void readConfiguration(final CompoundTag tag, final HolderLookup.Provider provider) {
        super.readConfiguration(tag, provider);
        filter.load(tag, provider);
    }

    @Override
    public ResourceContainerData getMenuData() {
        return ResourceContainerData.of(filter.getFilterContainer());
    }

    @Override
    public StreamEncoder<RegistryFriendlyByteBuf, ResourceContainerData> getMenuCodec() {
        return ResourceContainerData.STREAM_CODEC;
    }

    @Override
    public Component getName() {
        return overrideName(ContentNames.STORAGE_MONITOR);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(final int syncId, final Inventory inventory, final Player player) {
        return new StorageMonitorContainerMenu(syncId, player, this, filter.getFilterContainer());
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(final HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        tag.put(TAG_CLIENT_FILTER, filter.getFilterContainer().toTag(provider));
        tag.putLong(TAG_CLIENT_AMOUNT, currentAmount);
        tag.putBoolean(TAG_CLIENT_ACTIVE, currentlyActive);
        return tag;
    }

    private void sendDisplayUpdate() {
        if (level == null) {
            return;
        }
        sendDisplayUpdate(level, getAmount(), mainNetworkNode.isActive());
    }

    private void sendDisplayUpdate(final Level level, final long amount, final boolean active) {
        currentAmount = amount;
        currentlyActive = active;
        LOGGER.debug("Sending display update for storage monitor {} with amount {}", worldPosition, amount);
        PlatformUtil.sendBlockUpdateToClient(level, worldPosition);
    }

    @Override
    protected boolean doesBlockStateChangeWarrantNetworkNodeUpdate(final BlockState oldBlockState,
                                                                   final BlockState newBlockState) {
        return AbstractDirectionalBlock.didDirectionChange(oldBlockState, newBlockState);
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount,
                                                           final CancellationToken cancellationToken) {
        return Optional.ofNullable(mainNetworkNode.getNetwork())
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .map(component -> component.getPreview(resource, amount, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(Optional.empty()));
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        return Optional.ofNullable(mainNetworkNode.getNetwork())
            .map(network -> network.getComponent(AutocraftingNetworkComponent.class))
            .map(component -> component.getMaxAmount(resource, cancellationToken))
            .orElseGet(() -> CompletableFuture.completedFuture(0L));
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify,
                                                         final CancellationToken cancellationToken) {
        final Network network = mainNetworkNode.getNetwork();
        if (network == null) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return network.getComponent(AutocraftingNetworkComponent.class).startTask(resource, amount, actor, notify,
            cancellationToken);
    }

    @Override
    public void cancel() {
        final Network network = mainNetworkNode.getNetwork();
        if (network != null) {
            network.getComponent(AutocraftingNetworkComponent.class).cancel();
        }
    }
}
