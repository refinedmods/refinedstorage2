package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.abstractions.Platform;
import com.refinedmods.refinedstorage2.platform.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.BaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSynchronizationType;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.GridSearchBox;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;

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
    protected GridBlockEntity<T> grid;
    protected StorageChannel<T> storageChannel; // TODO - Support changing of the channel.
    private Runnable sizeChangedListener;

    private GridSize size;

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

        addDataSlot(redstoneModeProperty);
        addDataSlot(sortingDirectionProperty);
        addDataSlot(sortingTypeProperty);
        addDataSlot(sizeProperty);

        active = buf.readBoolean();

        this.view.setSortingDirection(GridSettings.getSortingDirection(buf.readInt()));
        this.view.setSortingType(GridSettings.getSortingType(buf.readInt()));
        size = GridSettings.getSize(buf.readInt());

        int amountOfResources = buf.readInt();
        for (int i = 0; i < amountOfResources; ++i) {
            ResourceAmount<T> resourceAmount = readResourceAmount(buf);
            StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);
            view.loadResource(resourceAmount.getResource(), resourceAmount.getAmount(), trackerEntry);
        }
        view.sort();

        addSlots(0);
    }

    protected GridContainerMenu(MenuType<?> type, int syncId, Inventory playerInventory, GridBlockEntity<T> grid, GridView<T> view) {
        super(type, syncId);

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

        addDataSlot(redstoneModeProperty);
        addDataSlot(sortingDirectionProperty);
        addDataSlot(sortingTypeProperty);
        addDataSlot(sizeProperty);

        this.playerInventory = playerInventory;
        this.storageChannel = grid.getNode().getStorageChannel();
        this.storageChannel.addListener(this);
        this.grid = grid;

        addSlots(0);
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

    public void setSearchBox(GridSearchBox searchBox) {
        searchBox.setAutoSelected(isAutoSelected());
        if (Platform.INSTANCE.getConfig().getGrid().isRememberSearchQuery()) {
            searchBox.setValue(lastSearchQuery);
            searchBox.addListener(text -> lastSearchQuery = text);
        }
    }

    @Override
    public void removed(Player playerEntity) {
        super.removed(playerEntity);

        if (storageChannel != null) {
            storageChannel.removeListener(this);
        }
    }

    public void addSlots(int playerInventoryY) {
        resetSlots();
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
            Platform.INSTANCE.getServerToClientCommunications().sendGridActiveness(serverPlayerEntity, active);
        }
    }

    public boolean isActive() {
        return active;
    }

    // TODO cache these values.
    public void setAutoSelected(boolean autoSelected) {
        Platform.INSTANCE.getConfig().getGrid().setAutoSelected(autoSelected);
    }

    public boolean isAutoSelected() {
        return Platform.INSTANCE.getConfig().getGrid().isAutoSelected();
    }

    public void setSynchronizationType(GridSynchronizationType type) {
        Platform.INSTANCE.getConfig().getGrid().setSynchronizationType(type.toConfig());
    }

    public GridSynchronizationType getSynchronizationType() {
        return GridSynchronizationType.ofConfig(Platform.INSTANCE.getConfig().getGrid().getSynchronizationType());
    }
}
