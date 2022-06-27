package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.list.listenable.ResourceListListener;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannel;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.apiimpl.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.RedstoneModeSettings;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.BaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.RedstoneModeAccessor;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.GridSearchBox;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
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

    @Nullable
    protected GridBlockEntity<T> grid;
    @Nullable
    protected StorageChannel<T> storageChannel; // TODO - Support changing of the channel.
    @Nullable
    private Runnable sizeChangedListener;

    private GridSynchronizer synchronizer;
    private boolean autoSelected;
    private boolean active;

    protected GridContainerMenu(final MenuType<?> type, final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf, final GridView<T> view) {
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

        this.active = buf.readBoolean();

        this.view.setSortingDirection(Platform.INSTANCE.getConfig().getGrid().getSortingDirection());
        this.view.setSortingType(Platform.INSTANCE.getConfig().getGrid().getSortingType());

        int amountOfResources = buf.readInt();
        for (int i = 0; i < amountOfResources; ++i) {
            ResourceAmount<T> resourceAmount = readResourceAmount(buf);
            TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
            view.loadResource(resourceAmount.getResource(), resourceAmount.getAmount(), trackedResource);
        }
        this.view.sort();

        addSlots(0);

        this.synchronizer = loadSynchronizer();
        this.autoSelected = loadAutoSelected();
    }

    protected GridContainerMenu(final MenuType<?> type, final int syncId, final Inventory playerInventory, final GridBlockEntity<T> grid, final GridView<T> view) {
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

        this.synchronizer = PlatformApi.INSTANCE.getGridSynchronizerRegistry().getDefault();
    }

    protected abstract ResourceAmount<T> readResourceAmount(FriendlyByteBuf buf);

    public void onResourceUpdate(final T template, final long amount, final TrackedResource trackedResource) {
        LOGGER.info("{} got updated with {}", template, amount);
        view.onChange(template, amount, trackedResource);
    }

    public void setSizeChangedListener(@Nullable final Runnable sizeChangedListener) {
        this.sizeChangedListener = sizeChangedListener;
    }

    public GridSortingDirection getSortingDirection() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingDirection();
    }

    public void setSortingDirection(final GridSortingDirection sortingDirection) {
        Platform.INSTANCE.getConfig().getGrid().setSortingDirection(sortingDirection);
        view.setSortingDirection(sortingDirection);
        view.sort();
    }

    public GridSortingType getSortingType() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingType();
    }

    public void setSortingType(final GridSortingType sortingType) {
        Platform.INSTANCE.getConfig().getGrid().setSortingType(sortingType);
        view.setSortingType(sortingType);
        view.sort();
    }

    public GridSize getSize() {
        return Platform.INSTANCE.getConfig().getGrid().getSize();
    }

    public void setSize(final GridSize size) {
        Platform.INSTANCE.getConfig().getGrid().setSize(size);
        if (sizeChangedListener != null) {
            sizeChangedListener.run();
        }
    }

    public void setSearchBox(final GridSearchBox searchBox) {
        searchBox.setAutoSelected(isAutoSelected());
        if (Platform.INSTANCE.getConfig().getGrid().isRememberSearchQuery()) {
            searchBox.setValue(lastSearchQuery);
            searchBox.addListener(GridContainerMenu::updateLastSearchQuery);
        }
    }

    private static void updateLastSearchQuery(final String text) {
        GridContainerMenu.lastSearchQuery = text;
    }

    @Override
    public void removed(Player playerEntity) {
        super.removed(playerEntity);

        if (storageChannel != null) {
            storageChannel.removeListener(this);
        }
    }

    public void addSlots(final int playerInventoryY) {
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
    public void setRedstoneMode(final RedstoneMode redstoneMode) {
        redstoneModeProperty.syncToServer(redstoneMode);
    }

    @Override
    public void onActiveChanged(final boolean active) {
        this.active = active;
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            Platform.INSTANCE.getServerToClientCommunications().sendGridActiveness(serverPlayerEntity, active);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setAutoSelected(final boolean autoSelected) {
        this.autoSelected = autoSelected;
        Platform.INSTANCE.getConfig().getGrid().setAutoSelected(autoSelected);
    }

    private boolean loadAutoSelected() {
        return Platform.INSTANCE.getConfig().getGrid().isAutoSelected();
    }

    public boolean isAutoSelected() {
        return autoSelected;
    }

    private GridSynchronizer loadSynchronizer() {
        return Platform.INSTANCE
                .getConfig()
                .getGrid()
                .getSynchronizer()
                .flatMap(id -> PlatformApi.INSTANCE.getGridSynchronizerRegistry().get(id))
                .orElse(PlatformApi.INSTANCE.getGridSynchronizerRegistry().getDefault());
    }

    public GridSynchronizer getSynchronizer() {
        return synchronizer;
    }

    public void toggleSynchronizer() {
        final OrderedRegistry<ResourceLocation, GridSynchronizer> synchronizerRegistry = PlatformApi.INSTANCE.getGridSynchronizerRegistry();
        final Config.Grid config = Platform.INSTANCE.getConfig().getGrid();

        final GridSynchronizer newSynchronizer = synchronizerRegistry.next(getSynchronizer());

        if (newSynchronizer == synchronizerRegistry.getDefault()) {
            config.clearSynchronizer();
        } else {
            synchronizerRegistry.getId(newSynchronizer).ifPresent(config::setSynchronizer);
        }

        this.synchronizer = newSynchronizer;
    }
}
