package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

import com.refinedmods.refinedstorage2.api.grid.GridEventHandler;
import com.refinedmods.refinedstorage2.api.grid.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.api.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.api.grid.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.GridSize;
import com.refinedmods.refinedstorage2.api.grid.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.GridView;
import com.refinedmods.refinedstorage2.api.grid.GridViewImpl;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.api.stack.item.Rs2ItemStackIdentifier;
import com.refinedmods.refinedstorage2.api.stack.list.StackListImpl;
import com.refinedmods.refinedstorage2.api.stack.list.StackListResult;
import com.refinedmods.refinedstorage2.api.stack.list.listenable.StackListListener;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.api.util.ItemStacks;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridSettings;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.ClientGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.ServerGridEventHandler;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.query.FabricGridStackFactory;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.GridSearchBox;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.BaseScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import java.util.Optional;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GridScreenHandler extends BaseScreenHandler implements GridEventHandler, StackListListener<Rs2ItemStack>, RedstoneModeAccessor {
    private static final Logger LOGGER = LogManager.getLogger(GridScreenHandler.class);

    private static String lastSearchQuery = "";

    private final PlayerInventory playerInventory;
    private final GridView<Rs2ItemStack> itemView = new GridViewImpl<>(new FabricGridStackFactory(), Rs2ItemStackIdentifier::new, StackListImpl.createItemStackList());
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;
    private final TwoWaySyncProperty<GridSortingDirection> sortingDirectionProperty;
    private final TwoWaySyncProperty<GridSortingType> sortingTypeProperty;
    private final TwoWaySyncProperty<GridSize> sizeProperty;
    private final TwoWaySyncProperty<GridSearchBoxMode> searchBoxModeProperty;
    private GridBlockEntity grid;
    private StorageChannel<Rs2ItemStack> storageChannel; // TODO - Support changing of the channel.
    private final GridEventHandler eventHandler;
    private Runnable sizeChangedListener;
    private GridSearchBox searchBox;

    private GridSize size;
    private GridSearchBoxMode searchBoxMode;

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(Rs2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.playerInventory = playerInventory;

        this.redstoneModeProperty = TwoWaySyncProperty.forClient(
                0,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneMode.IGNORE,
                redstoneMode -> {
                }
        );
        this.sortingDirectionProperty = TwoWaySyncProperty.forClient(
                1,
                GridSettings::getSortingDirection,
                GridSettings::getSortingDirection,
                GridSortingDirection.ASCENDING,
                this::onSortingDirectionChanged
        );
        this.sortingTypeProperty = TwoWaySyncProperty.forClient(
                2,
                GridSettings::getSortingType,
                GridSettings::getSortingType,
                GridSortingType.QUANTITY,
                this::onSortingTypeChanged
        );
        this.sizeProperty = TwoWaySyncProperty.forClient(
                3,
                GridSettings::getSize,
                GridSettings::getSize,
                GridSize.STRETCH,
                this::onSizeChanged
        );
        this.searchBoxModeProperty = TwoWaySyncProperty.forClient(
                4,
                GridSearchBoxModeRegistry.INSTANCE::getId,
                GridSearchBoxModeRegistry.INSTANCE::get,
                GridSearchBoxModeRegistry.INSTANCE.getDefault(),
                this::onSearchBoxModeChanged
        );

        addProperty(redstoneModeProperty);
        addProperty(sortingDirectionProperty);
        addProperty(sortingTypeProperty);
        addProperty(sizeProperty);
        addProperty(searchBoxModeProperty);

        boolean active = buf.readBoolean();

        this.eventHandler = new ClientGridEventHandler(active, itemView);

        itemView.setSortingDirection(GridSettings.getSortingDirection(buf.readInt()));
        itemView.setSortingType(GridSettings.getSortingType(buf.readInt()));
        size = GridSettings.getSize(buf.readInt());
        searchBoxMode = GridSearchBoxModeRegistry.INSTANCE.get(buf.readInt());

        addSlots(0);

        int amountOfStacks = buf.readInt();
        for (int i = 0; i < amountOfStacks; ++i) {
            Rs2ItemStack stack = PacketUtil.readItemStack(buf, true);
            StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);
            itemView.loadStack(stack, stack.getAmount(), trackerEntry);
        }
        itemView.sort();
    }

    public GridScreenHandler(int syncId, PlayerInventory playerInventory, GridBlockEntity grid) {
        super(Rs2Mod.SCREEN_HANDLERS.getGrid(), syncId);

        this.redstoneModeProperty = TwoWaySyncProperty.forServer(
                0,
                RedstoneModeSettings::getRedstoneMode,
                RedstoneModeSettings::getRedstoneMode,
                grid::getRedstoneMode,
                grid::setRedstoneMode
        );
        this.sortingDirectionProperty = TwoWaySyncProperty.forServer(
                1,
                GridSettings::getSortingDirection,
                GridSettings::getSortingDirection,
                grid::getSortingDirection,
                grid::setSortingDirection
        );
        this.sortingTypeProperty = TwoWaySyncProperty.forServer(
                2,
                GridSettings::getSortingType,
                GridSettings::getSortingType,
                grid::getSortingType,
                grid::setSortingType
        );
        this.sizeProperty = TwoWaySyncProperty.forServer(
                3,
                GridSettings::getSize,
                GridSettings::getSize,
                grid::getSize,
                grid::setSize
        );
        this.searchBoxModeProperty = TwoWaySyncProperty.forServer(
                4,
                GridSearchBoxModeRegistry.INSTANCE::getId,
                GridSearchBoxModeRegistry.INSTANCE::get,
                grid::getSearchBoxMode,
                grid::setSearchBoxMode
        );

        addProperty(redstoneModeProperty);
        addProperty(sortingDirectionProperty);
        addProperty(sortingTypeProperty);
        addProperty(sizeProperty);
        addProperty(searchBoxModeProperty);

        this.playerInventory = playerInventory;
        this.storageChannel = grid.getContainer().getNode().getStorageChannel();
        this.storageChannel.addListener(this);
        this.eventHandler = new ServerGridEventHandler(grid.getContainer().getNode().isActive(), storageChannel, (ServerPlayerEntity) playerInventory.player);
        this.grid = grid;
        this.grid.addWatcher(eventHandler);

        addSlots(0);
    }

    public void setSizeChangedListener(Runnable sizeChangedListener) {
        this.sizeChangedListener = sizeChangedListener;
    }

    public GridSortingDirection getSortingDirection() {
        return sortingDirectionProperty.getDeserialized();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        sortingDirectionProperty.syncToServer(sortingDirection);
    }

    public GridSortingType getSortingType() {
        return sortingTypeProperty.getDeserialized();
    }

    public void setSortingType(GridSortingType sortingType) {
        sortingTypeProperty.syncToServer(sortingType);
    }

    public GridSize getSize() {
        return size;
    }

    public void setSize(GridSize size) {
        sizeProperty.syncToServer(size);
    }

    public GridSearchBoxMode getSearchBoxMode() {
        return searchBoxModeProperty.getDeserialized();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
        searchBoxModeProperty.syncToServer(searchBoxMode);
    }

    private void onSortingTypeChanged(GridSortingType sortingType) {
        if (itemView.getSortingType() != sortingType) {
            itemView.setSortingType(sortingType);
            itemView.sort();
        }
    }

    private void onSortingDirectionChanged(GridSortingDirection sortingDirection) {
        if (itemView.getSortingDirection() != sortingDirection) {
            itemView.setSortingDirection(sortingDirection);
            itemView.sort();
        }
    }

    private void onSizeChanged(GridSize size) {
        if (this.size != size) {
            this.size = size;
            if (sizeChangedListener != null) {
                sizeChangedListener.run();
            }
        }
    }

    private void onSearchBoxModeChanged(GridSearchBoxMode searchBoxMode) {
        if (this.searchBoxMode != searchBoxMode) {
            this.searchBoxMode = searchBoxMode;
            this.updateSearchBox();
        }
    }

    public void setSearchBox(GridSearchBox searchBox) {
        this.searchBox = searchBox;
        this.updateSearchBox();
        if (Rs2Config.get().getGrid().isRememberSearchQuery()) {
            this.searchBox.setText(lastSearchQuery);
        }
    }

    private void updateSearchBox() {
        this.searchBox.setAutoSelected(searchBoxMode.shouldAutoSelect());
        this.searchBox.setListener(text -> {
            if (Rs2Config.get().getGrid().isRememberSearchQuery()) {
                updateLastSearchQuery(text);
            }
            searchBox.setInvalid(!searchBoxMode.onTextChanged(itemView, text));
        });
    }

    private static void updateLastSearchQuery(String query) {
        lastSearchQuery = query;
    }

    @Override
    public void close(PlayerEntity playerEntity) {
        super.close(playerEntity);

        if (storageChannel != null) {
            storageChannel.removeListener(this);
        }

        if (grid != null) {
            grid.removeWatcher(eventHandler);
        }
    }

    public void addSlots(int playerInventoryY) {
        slots.clear();
        addPlayerInventory(playerInventory, 8, playerInventoryY);
    }

    @Override
    public void onInsertFromCursor(GridInsertMode mode) {
        eventHandler.onInsertFromCursor(mode);
    }

    @Override
    public Rs2ItemStack onInsertFromTransfer(Rs2ItemStack slotStack) {
        return eventHandler.onInsertFromTransfer(slotStack);
    }

    @Override
    public void onExtract(Rs2ItemStack stack, GridExtractMode mode) {
        eventHandler.onExtract(stack, mode);
    }

    @Override
    public ItemStack transferSlot(PlayerEntity playerEntity, int slotIndex) {
        if (!playerEntity.world.isClient()) {
            Slot slot = getSlot(slotIndex);
            if (slot.hasStack()) {
                Rs2ItemStack slotStack = ItemStacks.ofItemStack(slot.getStack());
                ItemStack resultingStack = ItemStacks.toItemStack(eventHandler.onInsertFromTransfer(slotStack));
                slot.setStack(resultingStack);
                sendContentUpdates();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void onItemUpdate(Rs2ItemStack template, long amount, StorageTracker.Entry trackerEntry) {
        eventHandler.onItemUpdate(template, amount, trackerEntry);
    }

    @Override
    public void onActiveChanged(boolean active) {
        eventHandler.onActiveChanged(active);
    }

    @Override
    public boolean isActive() {
        return eventHandler.isActive();
    }

    @Override
    public void onScroll(Rs2ItemStack stack, int slot, GridScrollMode mode) {
        eventHandler.onScroll(stack, slot, mode);
    }

    @Override
    public void onChanged(StackListResult<Rs2ItemStack> change) {
        LOGGER.info("Received a change of {} for {}", change.getChange(), change.getStack());

        ServerPacketUtil.sendToPlayer((ServerPlayerEntity) playerInventory.player, PacketIds.GRID_ITEM_UPDATE, buf -> {
            PacketUtil.writeItemStack(buf, change.getStack(), false);
            buf.writeLong(change.getChange());

            Optional<StorageTracker.Entry> entry = storageChannel.getTracker().getEntry(change.getStack());
            PacketUtil.writeTrackerEntry(buf, entry);
        });
    }

    public GridView<Rs2ItemStack> getItemView() {
        return itemView;
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneModeProperty.getDeserialized();
    }

    @Override
    public void setRedstoneMode(RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }

    public int getPlayerInventorySlotThatHasStack(ItemStack stack) {
        return playerInventory.getSlotWithStack(stack);
    }
}
