package com.refinedmods.refinedstorage2.platform.common.grid;

import com.refinedmods.refinedstorage2.api.grid.operations.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.operations.GridInsertMode;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage2.api.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;
import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
import com.refinedmods.refinedstorage2.api.grid.view.GridView;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilder;
import com.refinedmods.refinedstorage2.api.grid.view.GridViewBuilderImpl;
import com.refinedmods.refinedstorage2.api.grid.watcher.GridWatcher;
import com.refinedmods.refinedstorage2.api.resource.ResourceAmount;
import com.refinedmods.refinedstorage2.api.resource.ResourceKey;
import com.refinedmods.refinedstorage2.api.storage.TrackedResourceAmount;
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
import com.refinedmods.refinedstorage2.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
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

import java.util.List;
import java.util.Map;
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

import static java.util.Objects.requireNonNull;

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

    protected final Inventory playerInventory;

    private final GridView view;
    @Nullable
    private Grid grid;
    @Nullable
    private GridInsertionStrategy insertionStrategy;
    @Nullable
    private GridExtractionStrategy extractionStrategy;
    @Nullable
    private GridScrollingStrategy scrollingStrategy;
    @Nullable
    private Runnable sizeChangedListener;
    private GridSynchronizer synchronizer;
    @Nullable
    private ResourceType resourceTypeFilter;
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
        final int resources = buf.readInt();
        for (int i = 0; i < resources; ++i) {
            final ResourceLocation resourceTypeId = buf.readResourceLocation();
            final ResourceType resourceType = PlatformApi.INSTANCE
                .getResourceTypeRegistry()
                .get(resourceTypeId)
                .orElseThrow();
            readResource(resourceType, buf, viewBuilder);
        }
        this.view = viewBuilder.build();
        this.view.setSortingDirection(Platform.INSTANCE.getConfig().getGrid().getSortingDirection());
        this.view.setSortingType(Platform.INSTANCE.getConfig().getGrid().getSortingType());
        this.view.setFilterAndSort(filterStorageChannel());

        this.synchronizer = loadSynchronizer();
        this.resourceTypeFilter = loadResourceType();
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
            .getResourceTypeId()
            .flatMap(resourceTypeId -> PlatformApi.INSTANCE
                .getResourceTypeRegistry()
                .get(resourceTypeId)
                .map(type -> type.isGridResourceBelonging(gridResource))
            ).orElse(true);
    }

    private static GridViewBuilder createViewBuilder() {
        return new GridViewBuilderImpl(
            new CompositeGridResourceFactory(PlatformApi.INSTANCE.getResourceTypeRegistry()),
            GridSortingTypes.NAME,
            GridSortingTypes.QUANTITY
        );
    }

    public void onResourceUpdate(final ResourceKey resource,
                                 final long amount,
                                 @Nullable final TrackedResource trackedResource) {
        LOGGER.debug("{} got updated with {}", resource, amount);
        view.onChange(resource, amount, trackedResource);
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
    public void onChanged(
        final ResourceKey resource,
        final long change,
        @Nullable final TrackedResource trackedResource
    ) {
        if (!(resource instanceof PlatformResourceKey platformResource)) {
            return;
        }
        LOGGER.info("{} received a change of {} for {}", this, change, resource);
        Platform.INSTANCE.getServerToClientCommunications().sendGridUpdate(
            (ServerPlayer) playerInventory.player,
            platformResource,
            change,
            trackedResource
        );
    }

    @Override
    public void invalidate() {
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            initStrategies();
            Platform.INSTANCE.getServerToClientCommunications().sendGridClear(serverPlayer);
        }
    }

    private void initStrategies() {
        this.insertionStrategy = PlatformApi.INSTANCE.createGridInsertionStrategy(
            this,
            playerInventory.player,
            requireNonNull(grid)
        );
        this.extractionStrategy = PlatformApi.INSTANCE.createGridExtractionStrategy(
            this,
            playerInventory.player,
            requireNonNull(grid)
        );
        this.scrollingStrategy = PlatformApi.INSTANCE.createGridScrollingStrategy(
            this,
            playerInventory.player,
            requireNonNull(grid)
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
    private ResourceType loadResourceType() {
        return Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getResourceTypeId()
            .flatMap(id -> PlatformApi.INSTANCE.getResourceTypeRegistry().get(id))
            .orElse(null);
    }

    public GridSynchronizer getSynchronizer() {
        return synchronizer;
    }

    @Nullable
    public ResourceType getResourceType() {
        return resourceTypeFilter;
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

    public void toggleResourceType() {
        final PlatformRegistry<ResourceType> registry = PlatformApi.INSTANCE.getResourceTypeRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final ResourceType newResourceType = resourceTypeFilter == null
            ? registry.getDefault()
            : registry.nextOrNullIfLast(resourceTypeFilter);
        if (newResourceType == null) {
            config.clearResourceType();
        } else {
            registry.getId(newResourceType).ifPresent(config::setResourceTypeId);
        }
        this.resourceTypeFilter = newResourceType;
        this.view.sort();
    }

    @Override
    public boolean onInsert(final GridInsertMode insertMode, final boolean tryAlternatives) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        if (insertionStrategy == null) {
            return false;
        }
        return insertionStrategy.onInsert(insertMode, tryAlternatives);
    }

    @Override
    public boolean onExtract(final PlatformResourceKey resource,
                             final GridExtractMode extractMode,
                             final boolean cursor) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        if (extractionStrategy == null) {
            return false;
        }
        return extractionStrategy.onExtract(resource, extractMode, cursor);
    }

    @Override
    public boolean onScroll(final PlatformResourceKey resource, final GridScrollMode scrollMode, final int slotIndex) {
        if (grid != null && !grid.isGridActive()) {
            return false;
        }
        if (scrollingStrategy == null) {
            return false;
        }
        return scrollingStrategy.onScroll(resource, scrollMode, slotIndex);
    }

    @Override
    public boolean onTransfer(final int slotIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (!playerEntity.level().isClientSide() && grid != null && grid.isGridActive()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem() && insertionStrategy != null && canTransferSlot(slot)) {
                insertionStrategy.onTransfer(slot.index);
            }
        }
        return super.quickMoveStack(playerEntity, slotIndex);
    }

    protected boolean canTransferSlot(final Slot slot) {
        return true;
    }

    private static void readResource(final ResourceType type,
                                     final FriendlyByteBuf buf,
                                     final GridViewBuilder viewBuilder) {
        final ResourceKey resource = type.fromBuffer(buf);
        final long amount = buf.readLong();
        final TrackedResource trackedResource = PacketUtil.readTrackedResource(buf);
        viewBuilder.withResource(resource, amount, trackedResource);
    }

    public void onClear() {
        view.clear();
    }

    public static void writeScreenOpeningData(final Grid grid, final FriendlyByteBuf buf) {
        buf.writeBoolean(grid.isGridActive());
        final List<TrackedResourceAmount> resources = grid.getResources(PlayerActor.class);
        buf.writeInt(resources.size());
        resources.forEach(resource -> writeGridResource(resource, buf));
    }

    private static void writeGridResource(final TrackedResourceAmount trackedResourceAmount,
                                          final FriendlyByteBuf buf) {
        final ResourceAmount resourceAmount = trackedResourceAmount.resourceAmount();
        final PlatformResourceKey resource = (PlatformResourceKey) resourceAmount.getResource();
        final ResourceType resourceType = resource.getResourceType();
        final ResourceLocation resourceTypeId = PlatformApi.INSTANCE.getResourceTypeRegistry().getId(resourceType)
            .orElseThrow();
        buf.writeResourceLocation(resourceTypeId);
        resource.toBuffer(buf);
        buf.writeLong(resourceAmount.getAmount());
        PacketUtil.writeTrackedResource(buf, trackedResourceAmount.trackedResource());
    }
}
