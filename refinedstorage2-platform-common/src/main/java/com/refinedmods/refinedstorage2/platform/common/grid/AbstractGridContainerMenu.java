package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.grid.GridWatcher;
import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilderImpl;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.storage.PlayerActor;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.common.Config;
import com.refinedmods.refinedstorage2.platform.common.Platform;
import com.refinedmods.refinedstorage2.platform.common.grid.strategy.ClientGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.common.grid.strategy.ClientGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.common.grid.strategy.ClientGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.common.grid.view.CompositeGridResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.util.PacketUtil;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGridContainerMenu extends AbstractBaseContainerMenu
    implements GridWatcher, GridInsertionStrategy, GridExtractionStrategy, GridScrollingStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridContainerMenu.class);
    private static final GridQueryParserImpl QUERY_PARSER = new GridQueryParserImpl(
        LexerTokenMappings.DEFAULT_MAPPINGS,
        ParserOperatorMappings.DEFAULT_MAPPINGS,
        Map.of(
            "@", Set.of(GridResourceAttributeKeys.MOD_ID, GridResourceAttributeKeys.MOD_NAME),
            "$", Set.of(GridResourceAttributeKeys.TAGS),
            "#", Set.of(GridResourceAttributeKeys.TOOLTIP)
        )
    );

    private static String lastSearchQuery = "";

    private final Inventory playerInventory;
    private final GridView view;
    @Nullable
    private Grid grid;

    private GridInsertionStrategy insertionStrategy;
    private GridExtractionStrategy extractionStrategy;
    private GridScrollingStrategy scrollingStrategy;

    @Nullable
    private Runnable sizeChangedListener;

    private GridSynchronizer synchronizer;
    @Nullable
    private PlatformStorageChannelType<?> storageChannelTypeFilter;
    private boolean autoSelected;
    private boolean active;

    @Nullable
    private GridSearchBox searchBox;

    protected AbstractGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final FriendlyByteBuf buf
    ) {
        super(menuType, syncId);

        this.playerInventory = playerInventory;

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
        this.view.setFilterAndSort(filterStorageChannel());

        this.synchronizer = loadSynchronizer();
        this.storageChannelTypeFilter = loadStorageChannelType();
        this.insertionStrategy = new ClientGridInsertionStrategy();
        this.extractionStrategy = new ClientGridExtractionStrategy();
        this.scrollingStrategy = new ClientGridScrollingStrategy();
        this.autoSelected = loadAutoSelected();
    }

    protected AbstractGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final Grid grid
    ) {
        super(menuType, syncId);

        this.view = createViewBuilder().build();

        this.playerInventory = playerInventory;
        this.grid = grid;
        this.grid.addWatcher(this, PlayerActor.class);

        this.synchronizer = PlatformApi.INSTANCE.getGridSynchronizerRegistry().getDefault();
        initStrategies();
    }

    private Predicate<GridResource> filterStorageChannel() {
        return gridResource -> Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getStorageChannelType()
            .flatMap(storageChannelTypeId -> PlatformApi.INSTANCE
                .getStorageChannelTypeRegistry()
                .get(storageChannelTypeId)
                .map(type -> type.isGridResourceBelonging(gridResource))
            ).orElse(true);
    }

    private static GridViewBuilder createViewBuilder() {
        return new GridViewBuilderImpl(
            new CompositeGridResourceFactory(PlatformApi.INSTANCE.getStorageChannelTypeRegistry()),
            GridSortingTypes.NAME,
            GridSortingTypes.QUANTITY
        );
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

    public GridSortingTypes getSortingType() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingType();
    }

    public void setSortingType(final GridSortingTypes sortingType) {
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
        registerViewUpdatingListener(searchBox);
        configureSearchBox(searchBox);
    }

    private void registerViewUpdatingListener(final GridSearchBox theSearchBox) {
        theSearchBox.addListener(text -> {
            final boolean valid = onSearchTextChanged(text);
            theSearchBox.setValid(valid);
        });
    }

    private boolean onSearchTextChanged(final String text) {
        try {
            view.setFilterAndSort(QUERY_PARSER.parse(text).and(filterStorageChannel()));
            return true;
        } catch (GridQueryParserException e) {
            view.setFilterAndSort(resource -> false);
            return false;
        }
    }

    private void configureSearchBox(final GridSearchBox theSearchBox) {
        theSearchBox.setAutoSelected(isAutoSelected());
        if (Platform.INSTANCE.getConfig().getGrid().isRememberSearchQuery()) {
            theSearchBox.setValue(lastSearchQuery);
            theSearchBox.addListener(AbstractGridContainerMenu::updateLastSearchQuery);
        }
    }

    private static void updateLastSearchQuery(final String text) {
        lastSearchQuery = text;
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
        final T resource,
        final long change,
        @Nullable final TrackedResource trackedResource
    ) {
        if (!(storageChannelType instanceof PlatformStorageChannelType<T> platformStorageChannelType)) {
            return;
        }
        LOGGER.info("{} received a change of {} for {}", this, change, resource);
        Platform.INSTANCE.getServerToClientCommunications().sendGridUpdate(
            (ServerPlayer) playerInventory.player,
            platformStorageChannelType,
            resource,
            change,
            trackedResource
        );
    }

    @Override
    public void onNetworkChanged() {
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            initStrategies();
            Platform.INSTANCE.getServerToClientCommunications().sendGridClear(serverPlayer);
        }
    }

    private void initStrategies() {
        this.insertionStrategy = PlatformApi.INSTANCE.createGridInsertionStrategy(
            this,
            playerInventory.player,
            Objects.requireNonNull(grid)
        );
        this.extractionStrategy = PlatformApi.INSTANCE.createGridExtractionStrategy(
            this,
            playerInventory.player,
            Objects.requireNonNull(grid)
        );
        this.scrollingStrategy = PlatformApi.INSTANCE.createGridScrollingStrategy(
            this,
            playerInventory.player,
            Objects.requireNonNull(grid)
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

    @Nullable
    private PlatformStorageChannelType<?> loadStorageChannelType() {
        return Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getStorageChannelType()
            .flatMap(id -> PlatformApi.INSTANCE.getStorageChannelTypeRegistry().get(id))
            .orElse(null);
    }

    public GridSynchronizer getSynchronizer() {
        return synchronizer;
    }

    @Nullable
    public PlatformStorageChannelType<?> getStorageChannelType() {
        return storageChannelTypeFilter;
    }

    public void toggleSynchronizer() {
        final PlatformRegistry<GridSynchronizer> registry = PlatformApi.INSTANCE.getGridSynchronizerRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final GridSynchronizer newSynchronizer = registry.next(getSynchronizer());
        if (newSynchronizer == registry.getDefault()) {
            config.clearSynchronizer();
        } else {
            registry.getId(newSynchronizer).ifPresent(config::setSynchronizer);
        }
        this.synchronizer = newSynchronizer;
    }

    public void toggleStorageChannelType() {
        final PlatformRegistry<PlatformStorageChannelType<?>> registry =
            PlatformApi.INSTANCE.getStorageChannelTypeRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final PlatformStorageChannelType<?> newStorageChannelType = storageChannelTypeFilter == null
            ? registry.getDefault()
            : registry.nextOrNullIfLast(storageChannelTypeFilter);
        if (newStorageChannelType == null) {
            config.clearStorageChannelType();
        } else {
            registry.getId(newStorageChannelType).ifPresent(config::setStorageChannelType);
        }
        this.storageChannelTypeFilter = newStorageChannelType;
        this.view.sort();
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        return insertionStrategy.onInsert(insertMode, tryAlternatives);
    }

    @Override
    public <T> boolean onExtract(final PlatformStorageChannelType<T> storageChannelType,
                                 final T resource,
                                 final GridExtractMode extractMode,
                                 final boolean cursor) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        return extractionStrategy.onExtract(storageChannelType, resource, extractMode, cursor);
    }

    @Override
    public <T> boolean onScroll(final PlatformStorageChannelType<T> storageChannelType,
                                final T resource,
                                final GridScrollMode scrollMode,
                                final int slotIndex) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        return scrollingStrategy.onScroll(storageChannelType, resource, scrollMode, slotIndex);
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (!playerEntity.level().isClientSide() && grid != null && grid.isGridActive()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem()) {
                insertionStrategy.onTransfer(slot.index);
            }
        }
        return super.quickMoveStack(playerEntity, slotIndex);
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

    public void onClear() {
        view.clear();
    }
}
