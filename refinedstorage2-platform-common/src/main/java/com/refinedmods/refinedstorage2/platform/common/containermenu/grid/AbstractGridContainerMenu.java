package com.refinedmods.refinedstorage2.platform.common.containermenu.grid;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.AbstractGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSize;
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

public abstract class AbstractGridContainerMenu<T> extends AbstractBaseContainerMenu
    implements GridWatcher<T> {
    private static final Logger LOGGER = LogManager.getLogger();

    private static String lastSearchQuery = "";

    protected final Inventory playerInventory;
    protected final GridView<T> view;
    @Nullable
    protected AbstractGridBlockEntity<T> grid;
    @Nullable
    private Runnable sizeChangedListener;

    private GridSynchronizer synchronizer;
    private boolean autoSelected;
    private boolean active;

    @Nullable
    private GridSearchBox searchBox;

    protected AbstractGridContainerMenu(final MenuType<?> type,
                                        final int syncId,
                                        final Inventory playerInventory,
                                        final FriendlyByteBuf buf,
                                        final GridViewBuilder<T> viewBuilder) {
        super(type, syncId);

        this.playerInventory = playerInventory;

        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));

        this.active = buf.readBoolean();

        final int amountOfResources = buf.readInt();
        for (int i = 0; i < amountOfResources; ++i) {
            final ResourceAmount<T> resourceAmount = readResourceAmount(buf);
            final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
            viewBuilder.withResource(resourceAmount.getResource(), resourceAmount.getAmount(), trackedResource);
        }
        this.view = viewBuilder.build();
        this.view.setSortingDirection(Platform.INSTANCE.getConfig().getGrid().getSortingDirection());
        this.view.setSortingType(Platform.INSTANCE.getConfig().getGrid().getSortingType());
        this.view.sort();

        addSlots(0);

        this.synchronizer = loadSynchronizer();
        this.autoSelected = loadAutoSelected();
    }

    protected AbstractGridContainerMenu(final MenuType<?> type,
                                        final int syncId,
                                        final Inventory playerInventory,
                                        final AbstractGridBlockEntity<T> grid,
                                        final GridViewBuilder<T> viewBuilder) {
        super(type, syncId);

        this.view = viewBuilder.build();

        registerProperty(new ServerProperty<>(
            PropertyTypes.REDSTONE_MODE,
            grid::getRedstoneMode,
            grid::setRedstoneMode
        ));

        this.playerInventory = playerInventory;
        this.grid = grid;
        this.grid.addWatcher(this, PlayerActor.class);

        addSlots(0);

        this.synchronizer = PlatformApi.INSTANCE.getGridSynchronizerRegistry().getDefault();
    }

    protected abstract ResourceAmount<T> readResourceAmount(FriendlyByteBuf buf);

    public void onResourceUpdate(final T template, final long amount, @Nullable final TrackedResource trackedResource) {
        LOGGER.debug("{} got updated with {}", template, amount);
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
        this.searchBox = searchBox;
        searchBox.setAutoSelected(isAutoSelected());
        if (Platform.INSTANCE.getConfig().getGrid().isRememberSearchQuery()) {
            searchBox.setValue(lastSearchQuery);
            searchBox.addListener(AbstractGridContainerMenu::updateLastSearchQuery);
        }
    }

    private static void updateLastSearchQuery(final String text) {
        AbstractGridContainerMenu.lastSearchQuery = text;
    }

    @Override
    public void removed(final Player playerEntity) {
        super.removed(playerEntity);
        if (grid != null) {
            grid.removeWatcher(this);
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
    public void onActiveChanged(final boolean newActive) {
        this.active = newActive;
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            Platform.INSTANCE.getServerToClientCommunications().sendGridActiveness(serverPlayerEntity, newActive);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setAutoSelected(final boolean autoSelected) {
        this.autoSelected = autoSelected;
        Platform.INSTANCE.getConfig().getGrid().setAutoSelected(autoSelected);
        if (searchBox != null) {
            searchBox.setAutoSelected(autoSelected);
        }
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
        final OrderedRegistry<ResourceLocation, GridSynchronizer> synchronizerRegistry =
            PlatformApi.INSTANCE.getGridSynchronizerRegistry();
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
