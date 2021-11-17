package com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridSettings;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.BaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.search.PlatformSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.GridSearchBox;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.fabric.util.ServerPacketUtil;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GridContainerMenu<T> extends BaseContainerMenu implements ResourceListListener<T>, RedstoneModeAccessor, GridWatcher {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String lastSearchQuery = "";

    protected final Inventory playerInventory;
    protected final GridView<T> view;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;
    private final TwoWaySyncProperty<GridSortingDirection> sortingDirectionProperty;
    private final TwoWaySyncProperty<GridSortingType> sortingTypeProperty;
    private final TwoWaySyncProperty<GridSize> sizeProperty;
    private final TwoWaySyncProperty<PlatformSearchBoxModeImpl> searchBoxModeProperty;
    protected GridBlockEntity<T> grid;
    protected StorageChannel<T> storageChannel; // TODO - Support changing of the channel.
    private Runnable sizeChangedListener;
    private GridSearchBox searchBox;

    private GridSize size;
    private PlatformSearchBoxModeImpl searchBoxMode;

    private boolean active;

    protected GridContainerMenu(MenuType<?> type, int syncId, Inventory playerInventory, FriendlyByteBuf buf, GridView<T> view) {
        super(type, syncId);

        this.view = view;

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
                id -> (PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.get(id),
                (PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.getDefault(),
                this::onSearchBoxModeChanged
        );

        addDataSlot(redstoneModeProperty);
        addDataSlot(sortingDirectionProperty);
        addDataSlot(sortingTypeProperty);
        addDataSlot(sizeProperty);
        addDataSlot(searchBoxModeProperty);

        active = buf.readBoolean();

        this.view.setSortingDirection(GridSettings.getSortingDirection(buf.readInt()));
        this.view.setSortingType(GridSettings.getSortingType(buf.readInt()));
        size = GridSettings.getSize(buf.readInt());
        searchBoxMode = (PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.get(buf.readInt());

        int amountOfResources = buf.readInt();
        for (int i = 0; i < amountOfResources; ++i) {
            ResourceAmount<T> resourceAmount = readResourceAmount(buf);
            StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);
            view.loadResource(resourceAmount.getResource(), resourceAmount.getAmount(), trackerEntry);
        }
        view.sort();

        addSlots(0);
    }

    protected GridContainerMenu(MenuType<?> screenHandlerType, int syncId, Inventory playerInventory, GridBlockEntity<T> grid, GridView<T> view) {
        super(screenHandlerType, syncId);

        this.view = view;

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
                id -> (PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.get(id),
                () -> (PlatformSearchBoxModeImpl) grid.getSearchBoxMode(),
                grid::setSearchBoxMode
        );

        addDataSlot(redstoneModeProperty);
        addDataSlot(sortingDirectionProperty);
        addDataSlot(sortingTypeProperty);
        addDataSlot(sizeProperty);
        addDataSlot(searchBoxModeProperty);

        this.playerInventory = playerInventory;
        this.storageChannel = grid.getContainer().getNode().getStorageChannel();
        this.storageChannel.addListener(this);
        this.grid = grid;

        addSlots(0);
    }

    private static void updateLastSearchQuery(String query) {
        lastSearchQuery = query;
    }

    protected abstract ResourceAmount<T> readResourceAmount(FriendlyByteBuf buf);

    public void onResourceUpdate(T template, long amount, StorageTracker.Entry trackerEntry) {
        LOGGER.info("{} got updated with {}", template, amount);
        view.onChange(template, amount, trackerEntry);
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

    public PlatformSearchBoxModeImpl getSearchBoxMode() {
        return searchBoxModeProperty.getDeserialized();
    }

    public void setSearchBoxMode(PlatformSearchBoxModeImpl searchBoxMode) {
        searchBoxModeProperty.syncToServer(searchBoxMode);
    }

    private void onSortingTypeChanged(GridSortingType sortingType) {
        if (view.getSortingType() != sortingType) {
            view.setSortingType(sortingType);
            view.sort();
        }
    }

    private void onSortingDirectionChanged(GridSortingDirection sortingDirection) {
        if (view.getSortingDirection() != sortingDirection) {
            view.setSortingDirection(sortingDirection);
            view.sort();
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

    private void onSearchBoxModeChanged(PlatformSearchBoxModeImpl searchBoxMode) {
        if (this.searchBoxMode != searchBoxMode) {
            this.searchBoxMode = searchBoxMode;
            this.updateSearchBox();
        }
    }

    public void setSearchBox(GridSearchBox searchBox) {
        this.searchBox = searchBox;
        this.updateSearchBox();
        if (Rs2Config.get().getGrid().isRememberSearchQuery()) {
            this.searchBox.setValue(lastSearchQuery);
        }
    }

    private void updateSearchBox() {
        this.searchBox.setAutoSelected(searchBoxMode.isAutoSelected());
        this.searchBox.setListener(text -> {
            if (Rs2Config.get().getGrid().isRememberSearchQuery()) {
                updateLastSearchQuery(text);
            }
            searchBox.setInvalid(!searchBoxMode.onTextChanged(view, text));
        });
    }

    @Override
    public void removed(Player playerEntity) {
        super.removed(playerEntity);

        if (storageChannel != null) {
            storageChannel.removeListener(this);
        }
    }

    public void addSlots(int playerInventoryY) {
        slots.clear();
        addPlayerInventory(playerInventory, 8, playerInventoryY);
    }

    public GridView<T> getView() {
        return view;
    }

    @Override
    public RedstoneMode getRedstoneMode() {
        return redstoneModeProperty.getDeserialized();
    }

    @Override
    public void setRedstoneMode(RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }

    @Override
    public void onActiveChanged(boolean active) {
        this.active = active;
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            ServerPacketUtil.sendToPlayer(serverPlayerEntity, PacketIds.GRID_ACTIVE, buf -> buf.writeBoolean(active));
        }
    }

    public boolean isActive() {
        return active;
    }
}
