package com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.stack.Rs2Stack;
import com.refinedmods.refinedstorage2.api.stack.list.listenable.StackListListener;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageTracker;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Config;
import com.refinedmods.refinedstorage2.platform.fabric.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.grid.GridSettings;
import com.refinedmods.refinedstorage2.platform.fabric.screen.grid.GridSearchBox;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.BaseScreenHandler;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.fabric.util.PacketUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandlerType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class GridScreenHandler<T extends Rs2Stack> extends BaseScreenHandler implements StackListListener<T>, RedstoneModeAccessor {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String lastSearchQuery = "";

    protected final PlayerInventory playerInventory;
    protected final GridView<T> view;
    private final TwoWaySyncProperty<RedstoneMode> redstoneModeProperty;
    private final TwoWaySyncProperty<GridSortingDirection> sortingDirectionProperty;
    private final TwoWaySyncProperty<GridSortingType> sortingTypeProperty;
    private final TwoWaySyncProperty<GridSize> sizeProperty;
    private final TwoWaySyncProperty<GridSearchBoxMode> searchBoxModeProperty;
    protected GridBlockEntity<T> grid;
    protected StorageChannel<T> storageChannel; // TODO - Support changing of the channel.
    private Runnable sizeChangedListener;
    private GridSearchBox searchBox;

    private GridSize size;
    private GridSearchBoxMode searchBoxMode;

    private boolean active;

    public GridScreenHandler(ScreenHandlerType<?> screenHandlerType, int syncId, PlayerInventory playerInventory, PacketByteBuf buf, GridView<T> view) {
        super(screenHandlerType, syncId);

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
            GridSearchBoxModeRegistry.INSTANCE::get,
            GridSearchBoxModeRegistry.INSTANCE.getDefault(),
            this::onSearchBoxModeChanged
        );

        addProperty(redstoneModeProperty);
        addProperty(sortingDirectionProperty);
        addProperty(sortingTypeProperty);
        addProperty(sizeProperty);
        addProperty(searchBoxModeProperty);

        active = buf.readBoolean();

        this.view.setSortingDirection(GridSettings.getSortingDirection(buf.readInt()));
        this.view.setSortingType(GridSettings.getSortingType(buf.readInt()));
        size = GridSettings.getSize(buf.readInt());
        searchBoxMode = GridSearchBoxModeRegistry.INSTANCE.get(buf.readInt());

        int amountOfStacks = buf.readInt();
        for (int i = 0; i < amountOfStacks; ++i) {
            T stack = readStack(buf);
            StorageTracker.Entry trackerEntry = PacketUtil.readTrackerEntry(buf);
            view.loadStack(stack, stack.getAmount(), trackerEntry);
        }
        view.sort();

        addSlots(0);
    }

    protected abstract T readStack(PacketByteBuf buf);

    public GridScreenHandler(ScreenHandlerType<?> screenHandlerType, int syncId, PlayerInventory playerInventory, GridBlockEntity<T> grid, GridView<T> view) {
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
        this.grid = grid;

        addSlots(0);
    }

    public void onStackUpdate(T template, long amount, StorageTracker.Entry trackerEntry) {
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

    public GridSearchBoxMode getSearchBoxMode() {
        return searchBoxModeProperty.getDeserialized();
    }

    public void setSearchBoxMode(GridSearchBoxMode searchBoxMode) {
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
            searchBox.setInvalid(!searchBoxMode.onTextChanged(view, text));
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

    public int getPlayerInventorySlotThatHasStack(ItemStack stack) {
        return playerInventory.getSlotWithStack(stack);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }
}
