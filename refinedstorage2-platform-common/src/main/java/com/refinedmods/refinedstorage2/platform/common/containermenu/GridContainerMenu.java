package com.refinedmods.refinedstorage2.platform.common.containermenu;

import com.refinedmods.refinedstorage2.api.core.registry.OrderedRegistry;
import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.service.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilderImpl;
import com.refinedmods.refinedstorage2.api.resource.list.ResourceListOperationResult;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.block.entity.GridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ServerProperty;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.ClientGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSize;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.view.CompositeGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.GridSearchBox;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;
import com.refinedmods.refinedstorage2.platform.common.util.RedstoneMode;

import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GridContainerMenu extends AbstractBaseContainerMenu
    implements GridWatcher, GridInsertionStrategy, GridExtractionStrategy, GridScrollingStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridContainerMenu.class);

    private static String lastSearchQuery = "";

    protected final Inventory playerInventory;
    protected final GridView view;
    @Nullable
    protected GridBlockEntity grid;

    private final GridInsertionStrategy insertionStrategy;
    private final GridExtractionStrategy extractionStrategy;
    private final GridScrollingStrategy scrollingStrategy;

    @Nullable
    private Runnable sizeChangedListener;

    private GridSynchronizer synchronizer;
    private boolean autoSelected;
    private boolean active;

    @Nullable
    private GridSearchBox searchBox;

    public GridContainerMenu(final int syncId, final Inventory playerInventory, final FriendlyByteBuf buf) {
        super(Menus.INSTANCE.getGrid(), syncId);

        this.playerInventory = playerInventory;

        registerProperty(new ClientProperty<>(PropertyTypes.REDSTONE_MODE, RedstoneMode.IGNORE));

        this.active = buf.readBoolean();

        final GridViewBuilder viewBuilder = createViewBuilder();
        final int amountOfStorageChannels = buf.readInt();
        for (int i = 0; i < amountOfStorageChannels; ++i) {
            final ResourceLocation id = buf.readResourceLocation();
            final PlatformStorageChannelType<?> storageChannelType = PlatformApi.INSTANCE
                .getStorageChannelTypeRegistry()
                .get(id)
                .orElseThrow();
            readStorageChannelFromBuffer(storageChannelType, buf, viewBuilder);
        }
        this.view = viewBuilder.build();
        this.view.setSortingDirection(Platform.INSTANCE.getConfig().getGrid().getSortingDirection());
        this.view.setSortingType(Platform.INSTANCE.getConfig().getGrid().getSortingType());
        this.view.sort();

        addSlots(0);

        this.synchronizer = loadSynchronizer();
        this.insertionStrategy = new ClientGridInsertionStrategy();
        this.extractionStrategy = new ClientGridExtractionStrategy();
        this.scrollingStrategy = new ClientGridScrollingStrategy();
        this.autoSelected = loadAutoSelected();
    }

    public GridContainerMenu(final int syncId,
                             final Inventory playerInventory,
                             final GridBlockEntity grid) {
        super(Menus.INSTANCE.getGrid(), syncId);

        this.view = createViewBuilder().build();

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
        this.insertionStrategy = PlatformApi.INSTANCE.createGridInsertionStrategy(
            this,
            playerInventory.player,
            grid.getNode()
        );
        this.extractionStrategy = PlatformApi.INSTANCE.createGridExtractionStrategy(
            this,
            playerInventory.player,
            grid.getNode(),
            grid.getContainerExtractionSource()
        );
        this.scrollingStrategy = PlatformApi.INSTANCE.createGridScrollingStrategy(
            this,
            playerInventory.player,
            grid.getNode()
        );
    }

    private static GridViewBuilder createViewBuilder() {
        return new GridViewBuilderImpl(new CompositeGridResourceFactory(
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry()
        ));
    }

    public <T> void onResourceUpdate(final T template,
                                     final long amount,
                                     @Nullable final TrackedResource trackedResource) {
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
            searchBox.addListener(GridContainerMenu::updateLastSearchQuery);
        }
    }

    private static void updateLastSearchQuery(final String text) {
        GridContainerMenu.lastSearchQuery = text;
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

    public GridView getView() {
        return view;
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        this.active = newActive;
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            Platform.INSTANCE.getServerToClientCommunications().sendGridActiveness(serverPlayerEntity, newActive);
        }
    }

    @Override
    public <T> void onChanged(
        final StorageChannelType<T> storageChannelType,
        final ResourceListOperationResult<T> change,
        @Nullable final TrackedResource trackedResource
    ) {
        if (!(storageChannelType instanceof PlatformStorageChannelType<T> platformStorageChannelType)) {
            return;
        }
        final T resource = change.resourceAmount().getResource();
        LOGGER.debug("Received a change of {} for {}", change.change(), resource);
        Platform.INSTANCE.getServerToClientCommunications().sendGridUpdate(
            (ServerPlayer) playerInventory.player,
            platformStorageChannelType,
            resource,
            change.change(),
            trackedResource
        );
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
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();

        final GridSynchronizer newSynchronizer = synchronizerRegistry.next(getSynchronizer());

        if (newSynchronizer == synchronizerRegistry.getDefault()) {
            config.clearSynchronizer();
        } else {
            synchronizerRegistry.getId(newSynchronizer).ifPresent(config::setSynchronizer);
        }

        this.synchronizer = newSynchronizer;
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        return insertionStrategy.onInsert(insertMode, tryAlternatives);
    }

    @Override
    public <T> boolean onExtract(final PlatformStorageChannelType<T> storageChannelType,
                                 final T resource,
                                 final GridExtractMode extractMode,
                                 final boolean cursor) {
        return extractionStrategy.onExtract(storageChannelType, resource, extractMode, cursor);
    }

    @Override
    public <T> boolean onScroll(final PlatformStorageChannelType<T> storageChannelType,
                                final T resource,
                                final GridScrollMode scrollMode,
                                final int slotIndex) {
        return scrollingStrategy.onScroll(storageChannelType, resource, scrollMode, slotIndex);
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (!playerEntity.level.isClientSide()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                insertionStrategy.onTransfer(slot.getContainerSlot());
            }
        }
        return ItemStack.EMPTY;
    }

    private static <T> void readStorageChannelFromBuffer(final PlatformStorageChannelType<T> type,
                                                         final FriendlyByteBuf buf,
                                                         final GridViewBuilder viewBuilder) {
        final int size = buf.readInt();
        for (int i = 0; i < size; ++i) {
            final T resource = type.fromBuffer(buf);
            final long amount = buf.readLong();
            final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
            viewBuilder.withResource(resource, amount, trackedResource);
        }
    }
}
