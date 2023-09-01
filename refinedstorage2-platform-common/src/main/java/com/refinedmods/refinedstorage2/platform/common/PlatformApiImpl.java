package com.refinedmods.refinedstorage2.platform.common;

import com.refinedmods.refinedstorage2.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage2.api.network.Network;
import com.refinedmods.refinedstorage2.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponent;
import com.refinedmods.refinedstorage2.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkBuilderImpl;
import com.refinedmods.refinedstorage2.api.network.impl.NetworkFactory;
import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;
import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.blockentity.constructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.blockentity.destructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.blockentity.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage2.platform.api.grid.Grid;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.api.grid.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage2.platform.api.integration.recipemod.IngredientConverter;
import com.refinedmods.refinedstorage2.platform.api.item.EnergyItemHelper;
import com.refinedmods.refinedstorage2.platform.api.item.StorageContainerItemHelper;
import com.refinedmods.refinedstorage2.platform.api.network.node.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.api.network.node.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.api.registry.PlatformRegistry;
import com.refinedmods.refinedstorage2.platform.api.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceFactory;
import com.refinedmods.refinedstorage2.platform.api.resource.ResourceRendering;
import com.refinedmods.refinedstorage2.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.api.storage.type.StorageType;
import com.refinedmods.refinedstorage2.platform.api.upgrade.BuiltinUpgradeDestinations;
import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage2.platform.common.block.entity.wirelesstransmitter.CompositeWirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage2.platform.common.integration.recipemod.CompositeIngredientConverter;
import com.refinedmods.refinedstorage2.platform.common.internal.energy.ItemEnergyStorage;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CompositeGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CompositeGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.CompositeGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.NoOpGridSynchronizer;
import com.refinedmods.refinedstorage2.platform.common.internal.item.EnergyItemHelperImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.item.StorageContainerItemHelperImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.network.LevelConnectionProvider;
import com.refinedmods.refinedstorage2.platform.common.internal.registry.PlatformRegistryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.FluidResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.ItemResourceFactory;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.StorageTypes;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.BuiltinUpgradeDestinationsImpl;
import com.refinedmods.refinedstorage2.platform.common.internal.upgrade.UpgradeRegistryImpl;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.hint.GridInsertionHintsImpl;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.hint.ItemGridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.screen.grid.hint.SingleItemGridInsertionHint;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
        new PlatformRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), StorageTypes.ITEM);
    private final PlatformRegistry<PlatformStorageChannelType<?>> storageChannelTypeRegistry =
        new PlatformRegistryImpl<>(createIdentifier(ITEM_REGISTRY_KEY), StorageChannelTypes.ITEM);
    private final PlatformRegistry<GridSynchronizer> gridSynchronizerRegistry =
        new PlatformRegistryImpl<>(createIdentifier("off"), new NoOpGridSynchronizer());
    private final PlatformRegistry<ImporterTransferStrategyFactory> importerTransferStrategyRegistry =
        new PlatformRegistryImpl<>(createIdentifier("noop"),
            (level, pos, direction, upgradeState, amountOverride) -> (filter, actor, network) -> false);
    private final PlatformRegistry<ExporterTransferStrategyFactory> exporterTransferStrategyRegistry =
        new PlatformRegistryImpl<>(createIdentifier("noop"),
            (level, pos, direction, upgradeState, amountOverride, fuzzyMode) -> (resource, actor, network) -> false);
    private final UpgradeRegistry upgradeRegistry = new UpgradeRegistryImpl();
    private final BuiltinUpgradeDestinations builtinUpgradeDestinations = new BuiltinUpgradeDestinationsImpl();
    private final Queue<PlatformExternalStorageProviderFactory> externalStorageProviderFactories = new PriorityQueue<>(
        Comparator.comparingInt(PlatformExternalStorageProviderFactory::getPriority)
    );
    private final Queue<DestructorStrategyFactory> destructorStrategyFactories = new PriorityQueue<>(
        Comparator.comparingInt(DestructorStrategyFactory::getPriority)
    );
    private final Queue<ConstructorStrategyFactory> constructorStrategyFactories = new PriorityQueue<>(
        Comparator.comparingInt(ConstructorStrategyFactory::getPriority)
    );
    private final CompositeIngredientConverter compositeConverter = new CompositeIngredientConverter();
    private final StorageContainerItemHelper storageContainerItemHelper = new StorageContainerItemHelperImpl();
    private final List<GridInsertionStrategyFactory> gridInsertionStrategyFactories = new ArrayList<>();
    private final GridInsertionHintsImpl gridInsertionHints = new GridInsertionHintsImpl(
        new ItemGridInsertionHint(),
        new SingleItemGridInsertionHint()
    );
    private final List<GridExtractionStrategyFactory> gridExtractionStrategyFactories = new ArrayList<>();
    private final List<GridScrollingStrategyFactory> gridScrollingStrategyFactories = new ArrayList<>();
    private final ResourceFactory<ItemResource> itemResourceFactory = new ItemResourceFactory();
    private final ResourceFactory<FluidResource> fluidResourceFactory = new FluidResourceFactory();
    private final Set<ResourceFactory<?>> resourceFactories = new HashSet<>();
    private final Map<Class<?>, ResourceRendering<?>> resourceRenderingMap = new HashMap<>();
    private final CompositeWirelessTransmitterRangeModifier wirelessTransmitterRangeModifier =
        new CompositeWirelessTransmitterRangeModifier();
    private final EnergyItemHelper energyItemHelper = new EnergyItemHelperImpl();

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
    public StorageContainerItemHelper getStorageContainerItemHelper() {
        return storageContainerItemHelper;
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
    public void addExternalStorageProviderFactory(final PlatformExternalStorageProviderFactory factory) {
        externalStorageProviderFactories.add(factory);
    }

    @Override
    public Collection<PlatformExternalStorageProviderFactory> getExternalStorageProviderFactories() {
        return externalStorageProviderFactories;
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
    public BuiltinUpgradeDestinations getBuiltinUpgradeDestinations() {
        return builtinUpgradeDestinations;
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
                                                             final Grid grid) {
        return new CompositeGridInsertionStrategy(
            Platform.INSTANCE.getDefaultGridInsertionStrategyFactory().create(
                containerMenu,
                player,
                grid
            ),
            gridInsertionStrategyFactories.stream().map(f -> f.create(
                containerMenu,
                player,
                grid
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
                                                               final Grid grid) {
        final List<GridExtractionStrategy> strategies = gridExtractionStrategyFactories
            .stream()
            .map(f -> f.create(containerMenu, player, grid))
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
                                                             final Grid grid) {
        final List<GridScrollingStrategy> strategies = gridScrollingStrategyFactories
            .stream()
            .map(f -> f.create(containerMenu, player, grid))
            .toList();
        return new CompositeGridScrollingStrategy(strategies);
    }

    @Override
    public void addGridScrollingStrategyFactory(final GridScrollingStrategyFactory scrollingStrategyFactory) {
        gridScrollingStrategyFactories.add(scrollingStrategyFactory);
    }

    @Override
    public <T> void addResourceFactory(final ResourceFactory<T> factory) {
        resourceFactories.add(factory);
    }

    @Override
    public ResourceFactory<ItemResource> getItemResourceFactory() {
        return itemResourceFactory;
    }

    @Override
    public PlatformStorageChannelType<ItemResource> getItemStorageChannelType() {
        return StorageChannelTypes.ITEM;
    }

    @Override
    public StorageType<ItemResource> getItemStorageType() {
        return StorageTypes.ITEM;
    }

    @Override
    public ResourceFactory<FluidResource> getFluidResourceFactory() {
        return fluidResourceFactory;
    }

    @Override
    public PlatformStorageChannelType<FluidResource> getFluidStorageChannelType() {
        return StorageChannelTypes.FLUID;
    }

    @Override
    public StorageType<FluidResource> getFluidStorageType() {
        return StorageTypes.FLUID;
    }

    @Override
    public Set<ResourceFactory<?>> getAlternativeResourceFactories() {
        return resourceFactories;
    }

    @Override
    public <T> void registerResourceRendering(final Class<T> resourceClass, final ResourceRendering<T> rendering) {
        resourceRenderingMap.put(resourceClass, rendering);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> ResourceRendering<T> getResourceRendering(final T resource) {
        return (ResourceRendering<T>) resourceRenderingMap.get(resource.getClass());
    }

    @Override
    public void registerIngredientConverter(final IngredientConverter converter) {
        this.compositeConverter.addConverter(converter);
    }

    @Override
    public IngredientConverter getIngredientConverter() {
        return compositeConverter;
    }

    @Override
    public void addWirelessTransmitterRangeModifier(final WirelessTransmitterRangeModifier rangeModifier) {
        wirelessTransmitterRangeModifier.addModifier(rangeModifier);
    }

    @Override
    public WirelessTransmitterRangeModifier getWirelessTransmitterRangeModifier() {
        return wirelessTransmitterRangeModifier;
    }

    @Override
    public Optional<EnergyStorage> getEnergyStorage(final ItemStack stack) {
        return Platform.INSTANCE.getEnergyStorage(stack);
    }

    @Override
    public EnergyItemHelper getEnergyItemHelper() {
        return energyItemHelper;
    }

    @Override
    public EnergyStorage asItemEnergyStorage(final EnergyStorage energyStorage,
                                             final ItemStack stack) {
        return new ItemEnergyStorage(stack, energyStorage);
    }
}
