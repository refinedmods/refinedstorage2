package com.refinedmods.refinedstorage.common.grid;

import com.refinedmods.refinedstorage.api.autocrafting.Pattern;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepository;
import com.refinedmods.refinedstorage.api.autocrafting.PatternRepositoryImpl;
import com.refinedmods.refinedstorage.api.autocrafting.calculation.CancellationToken;
import com.refinedmods.refinedstorage.api.autocrafting.preview.Preview;
import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.network.node.grid.GridExtractMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridInsertMode;
import com.refinedmods.refinedstorage.api.network.node.grid.GridWatcher;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryBuilder;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryBuilderImpl;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryFilter;
import com.refinedmods.refinedstorage.api.resource.repository.SortingDirection;
import com.refinedmods.refinedstorage.api.storage.Actor;
import com.refinedmods.refinedstorage.api.storage.tracked.TrackedResource;
import com.refinedmods.refinedstorage.common.Config;
import com.refinedmods.refinedstorage.common.Platform;
import com.refinedmods.refinedstorage.common.api.RefinedStorageApi;
import com.refinedmods.refinedstorage.common.api.autocrafting.CancelablePreviewProvider;
import com.refinedmods.refinedstorage.common.api.grid.Grid;
import com.refinedmods.refinedstorage.common.api.grid.GridScrollMode;
import com.refinedmods.refinedstorage.common.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.common.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.storage.PlayerActor;
import com.refinedmods.refinedstorage.common.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.autocrafting.PendingAutocraftingRequests;
import com.refinedmods.refinedstorage.common.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage.common.grid.query.GridQueryParserException;
import com.refinedmods.refinedstorage.common.grid.strategy.ClientGridExtractionStrategy;
import com.refinedmods.refinedstorage.common.grid.strategy.ClientGridInsertionStrategy;
import com.refinedmods.refinedstorage.common.grid.strategy.ClientGridScrollingStrategy;
import com.refinedmods.refinedstorage.common.support.containermenu.AbstractResourceContainerMenu;
import com.refinedmods.refinedstorage.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.common.support.resource.ResourceTypes;
import com.refinedmods.refinedstorage.common.support.stretching.ScreenSizeListener;
import com.refinedmods.refinedstorage.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage.query.parser.ParserOperatorMappings;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.requireNonNull;

public abstract class AbstractGridContainerMenu extends AbstractResourceContainerMenu
    implements GridWatcher, GridInsertionStrategy, GridExtractionStrategy, GridScrollingStrategy, ScreenSizeListener,
    CancelablePreviewProvider, GridSortingTypes.TrackedResourceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGridContainerMenu.class);
    private static final GridQueryParser QUERY_PARSER = new GridQueryParser(
        LexerTokenMappings.DEFAULT_MAPPINGS,
        ParserOperatorMappings.DEFAULT_MAPPINGS
    );

    private static String lastSearchQuery = "";

    protected final Inventory playerInventory;

    private final ResourceRepository<GridResource> repository;
    private final PatternRepository playerInventoryPatterns = new PatternRepositoryImpl();
    private final Map<ResourceKey, TrackedResource> trackedResources = new HashMap<>();
    @Nullable
    private Grid grid;
    @Nullable
    private GridInsertionStrategy insertionStrategy;
    @Nullable
    private GridExtractionStrategy extractionStrategy;
    @Nullable
    private GridScrollingStrategy scrollingStrategy;
    private GridSynchronizer synchronizer;
    @Nullable
    private ResourceType resourceTypeFilter;
    private boolean active;
    private final PendingAutocraftingRequests pendingAutocraftingRequests = new PendingAutocraftingRequests();

    protected AbstractGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final GridData gridData
    ) {
        super(menuType, syncId);

        this.playerInventory = playerInventory;

        this.active = gridData.active();

        final ResourceRepositoryBuilder<GridResource> repositoryBuilder = createRepositoryBuilder(this);
        gridData.resources().forEach(resource -> repositoryBuilder.addResource(
            resource.resourceAmount().resource(),
            resource.resourceAmount().amount()
        ));
        gridData.autocraftableResources().forEach(repositoryBuilder::addStickyResource);

        this.repository = repositoryBuilder.build();
        this.repository.setSort(
            Platform.INSTANCE.getConfig().getGrid().getSortingType().apply(this).apply(repository),
            Platform.INSTANCE.getConfig().getGrid().getSortingDirection()
        );
        this.repository.setFilterAndSort(createBaseFilter());

        this.synchronizer = loadSynchronizer();
        this.resourceTypeFilter = loadResourceType();
        this.insertionStrategy = new ClientGridInsertionStrategy();
        this.extractionStrategy = new ClientGridExtractionStrategy();
        this.scrollingStrategy = new ClientGridScrollingStrategy();
    }

    protected AbstractGridContainerMenu(
        final MenuType<? extends AbstractGridContainerMenu> menuType,
        final int syncId,
        final Inventory playerInventory,
        final Grid grid
    ) {
        super(menuType, syncId, playerInventory.player);

        this.repository = createRepositoryBuilder(this).build();

        this.playerInventory = playerInventory;
        this.grid = grid;
        this.grid.addWatcher(this, PlayerActor.class);

        this.synchronizer = NoopGridSynchronizer.INSTANCE;
        initStrategies((ServerPlayer) playerInventory.player);
    }

    private static ResourceRepositoryBuilder<GridResource> createRepositoryBuilder(
        final GridSortingTypes.TrackedResourceProvider sortingContext
    ) {
        return new ResourceRepositoryBuilderImpl<>(
            RefinedStorageApi.INSTANCE.getGridResourceRepositoryMapper(),
            GridSortingTypes.NAME.apply(sortingContext),
            GridSortingTypes.QUANTITY.apply(sortingContext)
        );
    }

    public void onResourceUpdate(final ResourceKey resource,
                                 final long amount,
                                 @Nullable final TrackedResource trackedResource) {
        LOGGER.debug("{} got updated with {}", resource, amount);
        repository.update(resource, amount);
        updateOrRemoveTrackedResource(resource, trackedResource);
    }

    @Override
    @Nullable
    public TrackedResource getTrackedResource(final GridResource resource) {
        return resource.getTrackedResource(this::getTrackedResource);
    }

    @Nullable
    public TrackedResource getTrackedResource(final ResourceKey resource) {
        return trackedResources.get(resource);
    }

    private void updateOrRemoveTrackedResource(final ResourceKey resource,
                                               @Nullable final TrackedResource trackedResource) {
        if (trackedResource == null) {
            trackedResources.remove(resource);
        } else {
            trackedResources.put(resource, trackedResource);
        }
    }

    public SortingDirection getSortingDirection() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingDirection();
    }

    public void setSortingDirection(final SortingDirection sortingDirection) {
        Platform.INSTANCE.getConfig().getGrid().setSortingDirection(sortingDirection);
        repository.setSort(
            Platform.INSTANCE.getConfig().getGrid().getSortingType().apply(this).apply(repository),
            sortingDirection
        );
        repository.sort();
    }

    public GridSortingTypes getSortingType() {
        return Platform.INSTANCE.getConfig().getGrid().getSortingType();
    }

    public void setSortingType(final GridSortingTypes sortingType) {
        Platform.INSTANCE.getConfig().getGrid().setSortingType(sortingType);
        repository.setSort(
            sortingType.apply(this).apply(repository),
            Platform.INSTANCE.getConfig().getGrid().getSortingDirection()
        );
        repository.sort();
    }

    public GridViewType getViewType() {
        return Platform.INSTANCE.getConfig().getGrid().getViewType();
    }

    public void setViewType(final GridViewType viewType) {
        Platform.INSTANCE.getConfig().getGrid().setViewType(viewType);
        repository.sort();
    }

    public void setSearchBox(final GridSearchBox searchBox) {
        searchBox.addListener(text -> {
            final boolean valid = onSearchTextChanged(text);
            searchBox.setValid(valid);
        });
        if (Platform.INSTANCE.getConfig().getGrid().isRememberSearchQuery()) {
            searchBox.setValue(lastSearchQuery);
            searchBox.addListener(AbstractGridContainerMenu::updateLastSearchQuery);
        }
    }

    private boolean onSearchTextChanged(final String text) {
        try {
            repository.setFilterAndSort(andFilter(QUERY_PARSER.parse(text), createBaseFilter()));
            return true;
        } catch (GridQueryParserException e) {
            repository.setFilterAndSort((v, resource) -> false);
            return false;
        }
    }

    private ResourceRepositoryFilter<GridResource> createBaseFilter() {
        return andFilter(createResourceTypeFilter(), createViewTypeFilter());
    }

    private ResourceRepositoryFilter<GridResource> createResourceTypeFilter() {
        return (v, resource) -> resource instanceof GridResource platformResource
            && Platform.INSTANCE.getConfig().getGrid().getResourceType().flatMap(resourceTypeId ->
            RefinedStorageApi.INSTANCE
                .getResourceTypeRegistry()
                .get(resourceTypeId)
                .map(platformResource::belongsToResourceType)
        ).orElse(true);
    }

    private ResourceRepositoryFilter<GridResource> createViewTypeFilter() {
        return (v, resource) -> Platform.INSTANCE.getConfig().getGrid().getViewType()
            .accepts(resource.isAutocraftable(v));
    }

    private static ResourceRepositoryFilter<GridResource> andFilter(final ResourceRepositoryFilter<GridResource> a,
                                                                    final ResourceRepositoryFilter<GridResource> b) {
        return (view, resource) -> a.test(view, resource) && b.test(view, resource);
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

    @Override
    public void resized(final int playerInventoryY, final int topYStart, final int topYEnd) {
        resetSlots();
        addPlayerInventory(playerInventory, 8, playerInventoryY, (before, after) -> {
            final Pattern beforePattern = RefinedStorageApi.INSTANCE.getPattern(before, playerInventory.player.level())
                .orElse(null);
            final Pattern afterPattern = RefinedStorageApi.INSTANCE.getPattern(after, playerInventory.player.level())
                .orElse(null);
            if (beforePattern != null) {
                playerInventoryPatterns.remove(beforePattern);
            }
            if (afterPattern != null) {
                playerInventoryPatterns.add(afterPattern, 0);
            }
        });
    }

    public ResourceRepository<GridResource> getRepository() {
        return repository;
    }

    @Override
    public void onActiveChanged(final boolean newActive) {
        this.active = newActive;
        if (this.playerInventory.player instanceof ServerPlayer serverPlayerEntity) {
            S2CPackets.sendGridActive(serverPlayerEntity, newActive);
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
        LOGGER.debug("{} received a change of {} for {}", this, change, resource);
        S2CPackets.sendGridUpdate(
            (ServerPlayer) playerInventory.player,
            platformResource,
            change,
            trackedResource
        );
    }

    @Override
    public void invalidate() {
        if (playerInventory.player instanceof ServerPlayer serverPlayer) {
            initStrategies(serverPlayer);
            S2CPackets.sendGridClear(serverPlayer);
        }
    }

    private void initStrategies(final ServerPlayer player) {
        this.insertionStrategy = RefinedStorageApi.INSTANCE.createGridInsertionStrategy(
            this,
            player,
            requireNonNull(grid)
        );
        this.extractionStrategy = RefinedStorageApi.INSTANCE.createGridExtractionStrategy(
            this,
            player,
            requireNonNull(grid)
        );
        this.scrollingStrategy = RefinedStorageApi.INSTANCE.createGridScrollingStrategy(
            this,
            player,
            requireNonNull(grid)
        );
    }

    public boolean isActive() {
        return active;
    }

    private GridSynchronizer loadSynchronizer() {
        return Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getSynchronizer()
            .flatMap(id -> RefinedStorageApi.INSTANCE.getGridSynchronizerRegistry().get(id))
            .orElse(NoopGridSynchronizer.INSTANCE);
    }

    @Nullable
    private ResourceType loadResourceType() {
        return Platform.INSTANCE
            .getConfig()
            .getGrid()
            .getResourceType()
            .flatMap(id -> RefinedStorageApi.INSTANCE.getResourceTypeRegistry().get(id))
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
        final PlatformRegistry<GridSynchronizer> registry = RefinedStorageApi.INSTANCE.getGridSynchronizerRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final GridSynchronizer newSynchronizer = registry.nextOrNullIfLast(getSynchronizer());
        if (newSynchronizer == null) {
            config.clearSynchronizer();
        } else {
            registry.getId(newSynchronizer).ifPresent(config::setSynchronizer);
        }
        this.synchronizer = newSynchronizer == null ? NoopGridSynchronizer.INSTANCE : newSynchronizer;
    }

    public void toggleResourceType() {
        final PlatformRegistry<ResourceType> registry = RefinedStorageApi.INSTANCE.getResourceTypeRegistry();
        final Config.GridEntry config = Platform.INSTANCE.getConfig().getGrid();
        final ResourceType newResourceType = resourceTypeFilter == null
            ? ResourceTypes.ITEM
            : registry.nextOrNullIfLast(resourceTypeFilter);
        if (newResourceType == null) {
            config.clearResourceType();
        } else {
            registry.getId(newResourceType).ifPresent(config::setResourceType);
        }
        this.resourceTypeFilter = newResourceType;
        this.repository.sort();
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

    @SuppressWarnings("resource")
    @Override
    public ItemStack quickMoveStack(final Player playerEntity, final int slotIndex) {
        if (transferManager.transfer(slotIndex)) {
            return ItemStack.EMPTY;
        } else if (!playerEntity.level().isClientSide() && grid != null && grid.isGridActive()) {
            final Slot slot = getSlot(slotIndex);
            if (slot.hasItem() && insertionStrategy != null && canTransferSlot(slot)) {
                insertionStrategy.onTransfer(slot.index);
            }
        }
        return ItemStack.EMPTY;
    }

    protected boolean canTransferSlot(final Slot slot) {
        return true;
    }

    public void onClear() {
        repository.clear();
        trackedResources.clear();
    }

    @Nullable
    public final AutocraftableResourceHint getAutocraftableResourceHint(final Slot slot) {
        final ResourceKey resource = getResourceForAutocraftableHint(slot);
        if (resource == null) {
            return null;
        }
        return getAutocraftableResourceHint(resource);
    }

    @Nullable
    private AutocraftableResourceHint getAutocraftableResourceHint(final ResourceKey resource) {
        if (repository.isSticky(resource)) {
            return AutocraftableResourceHint.AUTOCRAFTABLE;
        }
        if (playerInventoryPatterns.getOutputs().contains(resource)) {
            return AutocraftableResourceHint.PATTERN_IN_INVENTORY;
        }
        return null;
    }

    @Nullable
    protected ResourceKey getResourceForAutocraftableHint(final Slot slot) {
        return null;
    }

    @Override
    public CompletableFuture<Optional<Preview>> getPreview(final ResourceKey resource, final long amount,
                                                           final CancellationToken cancellationToken) {
        final CompletableFuture<Optional<Preview>> previewRequest = requireNonNull(grid).getPreview(resource, amount,
            cancellationToken);
        pendingAutocraftingRequests.add(previewRequest, cancellationToken);
        return previewRequest;
    }

    @Override
    public CompletableFuture<Long> getMaxAmount(final ResourceKey resource, final CancellationToken cancellationToken) {
        final CompletableFuture<Long> maxAmountRequest = requireNonNull(grid).getMaxAmount(resource, cancellationToken);
        pendingAutocraftingRequests.add(maxAmountRequest, cancellationToken);
        return maxAmountRequest;
    }

    @Override
    public CompletableFuture<Optional<TaskId>> startTask(final ResourceKey resource,
                                                         final long amount,
                                                         final Actor actor,
                                                         final boolean notify,
                                                         final CancellationToken cancellationToken) {
        final CompletableFuture<Optional<TaskId>> taskRequest = requireNonNull(grid).startTask(resource, amount, actor,
            notify, cancellationToken);
        pendingAutocraftingRequests.add(taskRequest, cancellationToken);
        return taskRequest;
    }

    @Override
    public void cancel() {
        pendingAutocraftingRequests.cancelAll();
    }

    public boolean isLargeSlot(final Slot slot) {
        return false;
    }
}
