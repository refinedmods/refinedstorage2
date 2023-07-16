package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.grid.service.GridServiceFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkBuilderImpl;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkFactory;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.api.storage.Storage;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.blockentity.constructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.blockentity.destructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.grid.PlatformGridServiceFactory;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerHelper;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.filter.FilteredResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.integration.recipemod.CompositeIngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CompositeGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CompositeGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CompositeGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.NoOpGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.PlatformGridServiceFactoryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.item.StorageContainerHelperImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.network.LevelConnectionProvider;
import com.refinedmods.refinedstorage2.platform.common.internal.registry.PlatformRegistryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.CompositeFilteredResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemFilteredResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeRegistryImpl;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.hint.GridInsertionHintsImpl;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.hint.ItemGridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.hint.SingleItemGridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class PlatformApiImpl implements PlatformApi {
    private static final String ITEM_REGISTRY_KEY = "item";

    private final StorageRepository clientStorageRepository =
        new ClientStorageRepository(Platform.INSTANCE.getClientToServerCommunications()::sendStorageInfoRequest);
    private final ComponentMapFactory<NetworkComponent, Network> networkComponentMapFactory =
        new ComponentMapFactory<>();
    private final NetworkBuilder networkBuilder =
        new NetworkBuilderImpl(new NetworkFactory(networkComponentMapFactory));
    private final PlatformRegistry<StorageType<?>> storageTypeRegistry =
        new PlatformRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), ItemStorageType.INSTANCE);
    private final PlatformRegistry<PlatformStorageChannelType<?>> storageChannelTypeRegistry =
        new PlatformRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), StorageChannelTypes.ITEM);
    private final PlatformRegistry<GridSynchronizer> gridSynchronizerRegistry =
        new PlatformRegistryImpl<>(createIdentifier("off"), new NoOpGridSynchronizer());
    private final PlatformRegistry<ImporterTransferStrategyFactory> importerTransferStrategyRegistry =
        new PlatformRegistryImpl<>(createIdentifier("noop"),
            (level, pos, direction, upgradeState) -> (filter, actor, network) -> false);
    private final PlatformRegistry<ExporterTransferStrategyFactory> exporterTransferStrategyRegistry =
        new PlatformRegistryImpl<>(createIdentifier("noop"),
            (level, pos, direction, upgradeState, amountOverride, fuzzyMode) -> (resource, actor, network) -> false);
    private final UpgradeRegistry upgradeRegistry = new UpgradeRegistryImpl();
    private final Map<StorageChannelType<?>, Queue<PlatformExternalStorageProviderFactory>>
        externalStorageProviderFactories = new HashMap<>();
    private final Queue<DestructorStrategyFactory> destructorStrategyFactories = new PriorityQueue<>(
        Comparator.comparingInt(DestructorStrategyFactory::getPriority)
    );
    private final Queue<ConstructorStrategyFactory> constructorStrategyFactories = new PriorityQueue<>(
        Comparator.comparingInt(ConstructorStrategyFactory::getPriority)
    );
    private final CompositeIngredientConverter compositeConverter = new CompositeIngredientConverter();
    private final StorageContainerHelper storageContainerHelper = new StorageContainerHelperImpl();
    private final List<GridInsertionStrategyFactory> gridInsertionStrategyFactories = new ArrayList<>();
    private final GridInsertionHintsImpl gridInsertionHints = new GridInsertionHintsImpl(
        new ItemGridInsertionHint(),
        new SingleItemGridInsertionHint()
    );
    private final List<GridExtractionStrategyFactory> gridExtractionStrategyFactories = new ArrayList<>();
    private final List<GridScrollingStrategyFactory> gridScrollingStrategyFactories = new ArrayList<>();
    private final CompositeFilteredResourceFactory filteredResourceFactory = new CompositeFilteredResourceFactory(
        new ItemFilteredResourceFactory()
    );

    @Override
    public PlatformRegistry<StorageType<?>> getStorageTypeRegistry() {
        return storageTypeRegistry;
    }

    @Override
    public StorageRepository getStorageRepository(final Level level) {
        if (level.getServer() == null) {
            return clientStorageRepository;
        }
        final ServerLevel serverLevel = Objects.requireNonNull(level.getServer().getLevel(Level.OVERWORLD));
        return serverLevel
            .getDataStorage()
            .computeIfAbsent(
                this::createStorageRepository,
                this::createStorageRepository,
                StorageRepositoryImpl.NAME
            );
    }

    @Override
    public StorageContainerHelper getStorageContainerHelper() {
        return storageContainerHelper;
    }

    private StorageRepositoryImpl createStorageRepository(final CompoundTag tag) {
        final StorageRepositoryImpl repository = createStorageRepository();
        repository.read(tag);
        return repository;
    }

    private StorageRepositoryImpl createStorageRepository() {
        return new StorageRepositoryImpl(storageTypeRegistry);
    }

    @Override
    public PlatformRegistry<PlatformStorageChannelType<?>> getStorageChannelTypeRegistry() {
        return storageChannelTypeRegistry;
    }

    @Override
    public PlatformRegistry<ImporterTransferStrategyFactory> getImporterTransferStrategyRegistry() {
        return importerTransferStrategyRegistry;
    }

    @Override
    public PlatformRegistry<ExporterTransferStrategyFactory> getExporterTransferStrategyRegistry() {
        return exporterTransferStrategyRegistry;
    }

    @Override
    public <T> void addExternalStorageProviderFactory(final StorageChannelType<T> channelType,
                                                      final PlatformExternalStorageProviderFactory factory) {
        final Queue<PlatformExternalStorageProviderFactory> factories =
            externalStorageProviderFactories.computeIfAbsent(
                channelType,
                k -> new PriorityQueue<>(Comparator.comparingInt(PlatformExternalStorageProviderFactory::getPriority))
            );
        factories.add(factory);
    }

    @Override
    public <T> Collection<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories(
        final StorageChannelType<T> channelType
    ) {
        final var factories = externalStorageProviderFactories.get(channelType);
        if (factories == null) {
            return Collections.emptyList();
        }
        return factories;
    }

    @Override
    public Collection<DestructorStrategyFactory> getDestructorStrategyFactories() {
        return destructorStrategyFactories;
    }

    @Override
    public void addDestructorStrategyFactory(final DestructorStrategyFactory factory) {
        destructorStrategyFactories.add(factory);
    }

    @Override
    public Collection<ConstructorStrategyFactory> getConstructorStrategyFactories() {
        return constructorStrategyFactories;
    }

    @Override
    public void addConstructorStrategyFactory(final ConstructorStrategyFactory factory) {
        constructorStrategyFactories.add(factory);
    }

    @Override
    public MutableComponent createTranslation(final String category, final String value, final Object... args) {
        return IdentifierUtil.createTranslation(category, value, args);
    }

    @Override
    public ComponentMapFactory<NetworkComponent, Network> getNetworkComponentMapFactory() {
        return networkComponentMapFactory;
    }

    @Override
    public PlatformRegistry<GridSynchronizer> getGridSynchronizerRegistry() {
        return gridSynchronizerRegistry;
    }

    @Override
    public UpgradeRegistry getUpgradeRegistry() {
        return upgradeRegistry;
    }

    @Override
    public void requestNetworkNodeInitialization(final NetworkNodeContainer container,
                                                 final Level level,
                                                 final Runnable callback) {
        final LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        TickHandler.runWhenReady(() -> {
            networkBuilder.initialize(container, connectionProvider);
            callback.run();
        });
    }

    @Override
    public void requestNetworkNodeRemoval(final NetworkNodeContainer container, final Level level) {
        final LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        networkBuilder.remove(container, connectionProvider);
    }

    @Override
    public void requestNetworkNodeUpdate(final NetworkNodeContainer container, final Level level) {
        final LevelConnectionProvider connectionProvider = new LevelConnectionProvider(level);
        networkBuilder.update(container, connectionProvider);
    }

    @Override
    public GridInsertionStrategy createGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                                             final Player player,
                                                             final GridServiceFactory gridServiceFactory) {
        final PlatformGridServiceFactory platformGridServiceFactory = new PlatformGridServiceFactoryImpl(
            gridServiceFactory
        );
        return new CompositeGridInsertionStrategy(
            Platform.INSTANCE.getDefaultGridInsertionStrategyFactory().create(
                containerMenu,
                player,
                platformGridServiceFactory
            ),
            gridInsertionStrategyFactories.stream().map(f -> f.create(
                containerMenu,
                player,
                platformGridServiceFactory
            )).toList()
        );
    }

    @Override
    public void addGridInsertionStrategyFactory(final GridInsertionStrategyFactory insertionStrategyFactory) {
        gridInsertionStrategyFactories.add(insertionStrategyFactory);
    }

    @Override
    public void addAlternativeGridInsertionHint(final GridInsertionHint hint) {
        gridInsertionHints.addAlternativeHint(hint);
    }

    @Override
    public GridInsertionHints getGridInsertionHints() {
        return gridInsertionHints;
    }

    @Override
    public GridExtractionStrategy createGridExtractionStrategy(final AbstractContainerMenu containerMenu,
                                                               final Player player,
                                                               final GridServiceFactory gridServiceFactory,
                                                               final Storage<ItemResource> itemStorage) {
        final PlatformGridServiceFactory platformGridServiceFactory = new PlatformGridServiceFactoryImpl(
            gridServiceFactory
        );
        final List<GridExtractionStrategy> strategies = gridExtractionStrategyFactories
            .stream()
            .map(f -> f.create(containerMenu, player, platformGridServiceFactory, itemStorage))
            .toList();
        return new CompositeGridExtractionStrategy(strategies);
    }

    @Override
    public void addGridExtractionStrategyFactory(final GridExtractionStrategyFactory extractionStrategyFactory) {
        gridExtractionStrategyFactories.add(extractionStrategyFactory);
    }

    @Override
    public GridScrollingStrategy createGridScrollingStrategy(final AbstractContainerMenu containerMenu,
                                                             final Player player,
                                                             final GridServiceFactory gridServiceFactory) {
        final PlatformGridServiceFactory platformGridServiceFactory = new PlatformGridServiceFactoryImpl(
            gridServiceFactory
        );
        final List<GridScrollingStrategy> strategies = gridScrollingStrategyFactories
            .stream()
            .map(f -> f.create(containerMenu, player, platformGridServiceFactory))
            .toList();
        return new CompositeGridScrollingStrategy(strategies);
    }

    @Override
    public void addGridScrollingStrategyFactory(final GridScrollingStrategyFactory scrollingStrategyFactory) {
        gridScrollingStrategyFactories.add(scrollingStrategyFactory);
    }

    @Override
    public void addFilteredResourceFactory(final FilteredResourceFactory factory) {
        filteredResourceFactory.addAlternativeFactory(factory);
    }

    @Override
    public FilteredResourceFactory getFilteredResourceFactory() {
        return filteredResourceFactory;
    }

    @Override
    public void registerIngredientConverter(final IngredientConverter converter) {
        this.compositeConverter.addConverter(converter);
    }

    @Override
    public IngredientConverter getIngredientConverter() {
        return compositeConverter;
    }
}
