package com.refinedmods.refinedstorage2.platform.common.storagemonitor;

import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.node.SimpleNetworkNode;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.FuzzyStorageChannel;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceAmountTemplate;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceContainer;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.ContentNames;
import com.refinedmods.refinedstorage2.platform.common.support.FilterWithFuzzyMode;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ExtendedMenuProvider;
import com.refinedmods.refinedstorage2.platform.common.support.network.AbstractRedstoneModeNetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.resource.ResourceContainerImpl;

import javax.annotation.Nullable;

import com.google.common.util.concurrent.RateLimiter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageMonitorBlockEntity extends AbstractRedstoneModeNetworkNodeContainerBlockEntity<SimpleNetworkNode>
    implements ExtendedMenuProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageMonitorBlockEntity.class);

    private static final String TAG_CLIENT_FILTER = "cf";
    private static final String TAG_CLIENT_AMOUNT = "ca";
    private static final String TAG_CLIENT_ACTIVE = "cac";

    private final FilterWithFuzzyMode filter;
    private final RateLimiter displayUpdateRateLimiter = RateLimiter.create(0.25);
    private final InsertTracker insertTracker = new InsertTracker();

    private long currentAmount;
    private boolean currentlyActive;

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
        final boolean active = getNode().isActive();
        if ((amount != currentAmount || active != currentlyActive) && displayUpdateRateLimiter.tryAcquire()) {
            sendDisplayUpdate(level, amount, active);
        }
    }

    private long getAmount() {
        final ResourceAmountTemplate<?> template = filter.getFilterContainer().get(0);
        if (template == null) {
            return 0;
        }
        final Network network = getNode().getNetwork();
        if (network == null) {
            return 0;
        }
        return getAmount(network, template);
    }

    private <T> long getAmount(final Network network, final ResourceAmountTemplate<T> template) {
        final StorageChannel<T> storageChannel = network.getComponent(StorageNetworkComponent.class)
            .getStorageChannel(template.getStorageChannelType());
        if (!filter.isFuzzyMode() || !(storageChannel instanceof FuzzyStorageChannel<T> fuzzyStorageChannel)) {
            return storageChannel.get(template.getResource()).map(ResourceAmount::getAmount).orElse(0L);
        }
        return fuzzyStorageChannel.getFuzzy(template.getResource()).stream().mapToLong(ResourceAmount::getAmount).sum();
    }

    public void extract(final Player player) {
        if (level == null) {
            return;
        }
        final Network network = getNode().getNetwork();
        if (network == null) {
            return;
        }
        final ResourceAmountTemplate<?> template = getFilteredResource();
        if (template == null) {
            return;
        }
        extract(level, player, template.getResource(), network);
    }

    private <T> void extract(
        final Level level,
        final Player player,
        final T template,
        final Network network
    ) {
        final boolean success = PlatformApi.INSTANCE.getStorageMonitorExtractionStrategy().extract(
            template,
            !player.isShiftKeyDown(),
            player,
            new PlayerActor(player),
            network
        );
        if (!success) {
            return;
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
    }

    public void insert(final Player player, final InteractionHand hand) {
        if (level != null && doInsert(player, hand)) {
            sendDisplayUpdate();
        }
    }

    private boolean doInsert(final Player player, final InteractionHand hand) {
        final Network network = getNode().getNetwork();
        if (network == null) {
            return false;
        }
        final ResourceAmountTemplate<?> template = getFilteredResource();
        if (template == null) {
            return false;
        }
        final ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.isEmpty()) {
            return doInsertAll(player);
        }
        return doInsert(player, hand, heldStack, template.getResource(), network);
    }

    private <T> boolean doInsert(
        final Player player,
        final InteractionHand hand,
        final ItemStack heldStack,
        final T template,
        final Network network
    ) {
        return PlatformApi.INSTANCE.getStorageMonitorInsertionStrategy().insert(
            template,
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
        final Network network = getNode().getNetwork();
        if (network == null) {
            return false;
        }
        final ResourceAmountTemplate<?> template = getFilteredResource();
        if (template == null) {
            return false;
        }
        boolean success = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); ++i) {
            success |= tryInsertSlot(player, lastInsertedItem, i, template.getResource(), network);
        }
        return success;
    }

    private <T> boolean tryInsertSlot(
        final Player player,
        final ItemResource lastInsertedItem,
        final int inventorySlotIndex,
        final T template,
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
        return PlatformApi.INSTANCE.getStorageMonitorInsertionStrategy().insert(
            template,
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
    public ResourceAmountTemplate<?> getFilteredResource() {
        return filter.getFilterContainer().get(0);
    }

    public long getCurrentAmount() {
        return currentAmount;
    }

    public boolean isCurrentlyActive() {
        return currentlyActive;
    }

    @Override
    public void writeConfiguration(final CompoundTag tag) {
        super.writeConfiguration(tag);
        filter.save(tag);
    }

    @Override
    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_CLIENT_FILTER) && tag.contains(TAG_CLIENT_AMOUNT) && tag.contains(TAG_CLIENT_ACTIVE)) {
            filter.getFilterContainer().fromTag(tag.getCompound(TAG_CLIENT_FILTER));
            currentAmount = tag.getLong(TAG_CLIENT_AMOUNT);
            currentlyActive = tag.getBoolean(TAG_CLIENT_ACTIVE);
        }
        super.load(tag);
    }

    @Override
    public void readConfiguration(final CompoundTag tag) {
        super.readConfiguration(tag);
        filter.load(tag);
    }

    @Override
    public void writeScreenOpeningData(final ServerPlayer player, final FriendlyByteBuf buf) {
        filter.getFilterContainer().writeToUpdatePacket(buf);
    }

    @Override
    public Component getDisplayName() {
        return ContentNames.STORAGE_MONITOR;
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
    public CompoundTag getUpdateTag() {
        final CompoundTag tag = new CompoundTag();
        tag.put(TAG_CLIENT_FILTER, filter.getFilterContainer().toTag());
        tag.putLong(TAG_CLIENT_AMOUNT, currentAmount);
        tag.putBoolean(TAG_CLIENT_ACTIVE, currentlyActive);
        return tag;
    }

    private void sendDisplayUpdate() {
        if (level == null) {
            return;
        }
        sendDisplayUpdate(level, getAmount(), getNode().isActive());
    }

    private void sendDisplayUpdate(final Level level, final long amount, final boolean active) {
        currentAmount = amount;
        currentlyActive = active;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        LOGGER.debug("Sending display update for storage monitor {} with amount {}", worldPosition, amount);
    }
}
