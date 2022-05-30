package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizationType;
import com.refinedmods.refinedstorage2.platform.api.network.node.RedstoneMode;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.BaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
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
    protected GridBlockEntity<T> grid;
    protected StorageChannel<T> storageChannel; // TODO - Support changing of the channel.
    private Runnable sizeChangedListener;

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

        addDataSlot(redstoneModeProperty);

        active = buf.readBoolean();

        this.view.setSortingDirection(Platform.INSTANCE.getConfig().getGrid().getSortingDirection());
        this.view.setSortingType(Platform.INSTANCE.getConfig().getGrid().getSortingType());

        int amountOfResources = buf.readInt();
        for (int i = 0; i < amountOfResources; ++i) {
            ResourceAmount<T> resourceAmount = readResourceAmount(buf);
            TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
            view.loadResource(resourceAmount.getResource(), resourceAmount.getAmount(), trackedResource);
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

        addDataSlot(redstoneModeProperty);

        this.playerInventory = playerInventory;
        this.storageChannel = grid.getNode().getStorageChannel();
        this.storageChannel.addListener(this);
        this.grid = grid;

        addSlots(0);
    }

    protected abstract ResourceAmount<T> readResourceAmount(FriendlyByteBuf buf);

    public void onResourceUpdate(T template, long amount, TrackedResource trackedResource) {
        LOGGER.info("{} got updated with {}", template, amount);
        view.onChange(template, amount, trackedResource);
    }

    public void setSizeChangedListener(Runnable sizeChangedListener) {
        this.sizeChangedListener = sizeChangedListener;
    }

    public GridSortingDirection getSortingDirection() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingDirection();
    }

    public void setSortingDirection(GridSortingDirection sortingDirection) {
        Platform.INSTANCE.getConfig().getGrid().setSortingDirection(sortingDirection);
        view.setSortingDirection(sortingDirection);
        view.sort();
    }

    public GridSortingType getSortingType() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingType();
    }

    public void setSortingType(GridSortingType sortingType) {
        Platform.INSTANCE.getConfig().getGrid().setSortingType(sortingType);
        view.setSortingType(sortingType);
        view.sort();
    }

    public GridSize getSize() {
        return Platform.INSTANCE.getConfig().getGrid().getSize();
    }

    public void setSize(GridSize size) {
        Platform.INSTANCE.getConfig().getGrid().setSize(size);
        if (sizeChangedListener != null) {
            sizeChangedListener.run();
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
        Platform.INSTANCE.getConfig().getGrid().setSynchronizationType(type);
    }

    public GridSynchronizationType getSynchronizationType() {
        return Platform.INSTANCE.getConfig().getGrid().getSynchronizationType();
    }
}
