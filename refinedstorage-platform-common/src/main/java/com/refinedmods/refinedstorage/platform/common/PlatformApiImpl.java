package com.refinedmods.refinedstorage.platform.common;

import com.refinedmods.refinedstorage.api.core.component.ComponentMapFactory;
import com.refinedmods.refinedstorage.api.network.Network;
import com.refinedmods.refinedstorage.api.network.NetworkBuilder;
import com.refinedmods.refinedstorage.api.network.NetworkComponent;
import com.refinedmods.refinedstorage.api.network.energy.EnergyStorage;
import com.refinedmods.refinedstorage.api.network.impl.NetworkBuilderImpl;
import com.refinedmods.refinedstorage.api.network.impl.NetworkFactory;
import com.refinedmods.refinedstorage.api.network.node.NetworkNode;
import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.ConstructorStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.constructordestructor.DestructorStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.exporter.ExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.grid.Grid;
import com.refinedmods.refinedstorage.platform.api.grid.GridInsertionHint;
import com.refinedmods.refinedstorage.platform.api.grid.GridInsertionHints;
import com.refinedmods.refinedstorage.platform.api.grid.GridSynchronizer;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridExtractionStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridInsertionStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.api.grid.strategy.GridScrollingStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.importer.ImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage.platform.api.security.BuiltinPermissions;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.api.storage.StorageContainerItemHelper;
import com.refinedmods.refinedstorage.platform.api.storage.StorageRepository;
import com.refinedmods.refinedstorage.platform.api.storage.StorageType;
import com.refinedmods.refinedstorage.platform.api.storage.externalstorage.PlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage.platform.api.storagemonitor.StorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage.platform.api.storagemonitor.StorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.platform.api.support.energy.EnergyItemHelper;
import com.refinedmods.refinedstorage.platform.api.support.network.ConnectionLogic;
import com.refinedmods.refinedstorage.platform.api.support.network.InWorldNetworkNodeContainer;
import com.refinedmods.refinedstorage.platform.api.support.network.NetworkNodeContainerBlockEntity;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.NetworkBoundItemHelper;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReference;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceFactory;
import com.refinedmods.refinedstorage.platform.api.support.network.bounditem.SlotReferenceProvider;
import com.refinedmods.refinedstorage.platform.api.support.registry.PlatformRegistry;
import com.refinedmods.refinedstorage.platform.api.support.resource.RecipeModIngredientConverter;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceFactory;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.platform.api.upgrade.BuiltinUpgradeDestinations;
import com.refinedmods.refinedstorage.platform.api.upgrade.UpgradeRegistry;
import com.refinedmods.refinedstorage.platform.api.wirelesstransmitter.WirelessTransmitterRangeModifier;
import com.refinedmods.refinedstorage.platform.common.grid.NoopGridSynchronizer;
import com.refinedmods.refinedstorage.platform.common.grid.screen.hint.GridInsertionHintsImpl;
import com.refinedmods.refinedstorage.platform.common.grid.screen.hint.ItemGridInsertionHint;
import com.refinedmods.refinedstorage.platform.common.grid.screen.hint.SingleItemGridInsertionHint;
import com.refinedmods.refinedstorage.platform.common.grid.strategy.CompositeGridExtractionStrategy;
import com.refinedmods.refinedstorage.platform.common.grid.strategy.CompositeGridInsertionStrategy;
import com.refinedmods.refinedstorage.platform.common.grid.strategy.CompositeGridScrollingStrategy;
import com.refinedmods.refinedstorage.platform.common.security.BuiltinPermission;
import com.refinedmods.refinedstorage.platform.common.storage.ClientStorageRepository;
import com.refinedmods.refinedstorage.platform.common.storage.StorageContainerItemHelperImpl;
import com.refinedmods.refinedstorage.platform.common.storage.StorageRepositoryImpl;
import com.refinedmods.refinedstorage.platform.common.storage.StorageTypes;
import com.refinedmods.refinedstorage.platform.common.storagemonitor.CompositeStorageMonitorExtractionStrategy;
import com.refinedmods.refinedstorage.platform.common.storagemonitor.CompositeStorageMonitorInsertionStrategy;
import com.refinedmods.refinedstorage.platform.common.support.energy.EnergyItemHelperImpl;
import com.refinedmods.refinedstorage.platform.common.support.energy.ItemBlockEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.energy.ItemEnergyStorage;
import com.refinedmods.refinedstorage.platform.common.support.network.ConnectionProviderImpl;
import com.refinedmods.refinedstorage.platform.common.support.network.InWorldNetworkNodeContainerImpl;
import com.refinedmods.refinedstorage.platform.common.support.network.bounditem.CompositeSlotReferenceProvider;
import com.refinedmods.refinedstorage.platform.common.support.network.bounditem.InventorySlotReference;
import com.refinedmods.refinedstorage.platform.common.support.network.bounditem.NetworkBoundItemHelperImpl;
import com.refinedmods.refinedstorage.platform.common.support.packet.c2s.C2SPackets;
import com.refinedmods.refinedstorage.platform.common.support.packet.s2c.S2CPackets;
import com.refinedmods.refinedstorage.platform.common.support.registry.PlatformRegistryImpl;
import com.refinedmods.refinedstorage.platform.common.support.resource.CompositeRecipeModIngredientConverter;
import com.refinedmods.refinedstorage.platform.common.support.resource.FluidResourceFactory;
import com.refinedmods.refinedstorage.platform.common.support.resource.ItemResourceFactory;
import com.refinedmods.refinedstorage.platform.common.upgrade.BuiltinUpgradeDestinationsImpl;
import com.refinedmods.refinedstorage.platform.common.upgrade.UpgradeRegistryImpl;
import com.refinedmods.refinedstorage.platform.common.util.ServerEventQueue;
import com.refinedmods.refinedstorage.platform.common.wirelesstransmitter.CompositeWirelessTransmitterRangeModifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;
import static java.util.Objects.requireNonNull;

public class PlatformApiImpl implements PlatformApi {
    private final StorageRepository clientStorageRepository = new ClientStorageRepository(
        C2SPackets::sendStorageInfoRequest
    );
    private final ComponentMapFactory<NetworkComponent, Network> networkComponentMapFactory =
        new ComponentMapFactory<>();
    private final NetworkBuilder networkBuilder =
        new NetworkBuilderImpl(new NetworkFactory(networkComponentMapFactory));
    private final PlatformRegistry<StorageType> storageTypeRegistry = new PlatformRegistryImpl<>();
    private final PlatformRegistry<ResourceType> resourceTypeRegistry = new PlatformRegistryImpl<>();
    private final PlatformRegistry<GridSynchronizer> gridSynchronizerRegistry = new PlatformRegistryImpl<>();
    private final PlatformRegistry<ImporterTransferStrategyFactory> importerTransferStrategyRegistry =
        new PlatformRegistryImpl<>();
    private final PlatformRegistry<ExporterTransferStrategyFactory> exporterTransferStrategyRegistry =
        new PlatformRegistryImpl<>();
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
    private final CompositeStorageMonitorInsertionStrategy storageMonitorInsertionStrategy =
        new CompositeStorageMonitorInsertionStrategy();
    private final CompositeStorageMonitorExtractionStrategy storageMonitorExtractionStrategy =
        new CompositeStorageMonitorExtractionStrategy();
    private final CompositeRecipeModIngredientConverter ingredientConverter =
        new CompositeRecipeModIngredientConverter();
    private final StorageContainerItemHelper storageContainerItemHelper = new StorageContainerItemHelperImpl();
    private final List<GridInsertionStrategyFactory> gridInsertionStrategyFactories = new ArrayList<>();
    private final GridInsertionHintsImpl gridInsertionHints = new GridInsertionHintsImpl(
        new ItemGridInsertionHint(),
        new SingleItemGridInsertionHint()
    );
    private final List<GridExtractionStrategyFactory> gridExtractionStrategyFactories = new ArrayList<>();
    private final List<GridScrollingStrategyFactory> gridScrollingStrategyFactories = new ArrayList<>();
    private final ResourceFactory itemResourceFactory = new ItemResourceFactory();
    private final ResourceFactory fluidResourceFactory = new FluidResourceFactory();
    private final Set<ResourceFactory> resourceFactories = new HashSet<>();
    private final Map<Class<?>, ResourceRendering> resourceRenderingMap = new HashMap<>();
    private final CompositeWirelessTransmitterRangeModifier wirelessTransmitterRangeModifier =
        new CompositeWirelessTransmitterRangeModifier();
    private final EnergyItemHelper energyItemHelper = new EnergyItemHelperImpl();
    private final NetworkBoundItemHelper networkBoundItemHelper = new NetworkBoundItemHelperImpl();
    private final PlatformRegistry<SlotReferenceFactory> slotReferenceFactoryRegistry = new PlatformRegistryImpl<>();
    private final CompositeSlotReferenceProvider slotReferenceProvider = new CompositeSlotReferenceProvider();
    private final PlatformRegistry<PlatformPermission> permissionRegistry = new PlatformRegistryImpl<>();

    public PlatformApiImpl() {
        gridSynchronizerRegistry.register(createIdentifier("off"), NoopGridSynchronizer.INSTANCE);
    }

    @Override
    public PlatformRegistry<StorageType> getStorageTypeRegistry() {
        return storageTypeRegistry;
    }

    @Override
    public StorageRepository getClientStorageRepository() {
        return clientStorageRepository;
    }

    @SuppressWarnings("DataFlowIssue") // NeoForge makes null datafixer safe
    @Override
    public StorageRepository getStorageRepository(final Level level) {
        final ServerLevel serverLevel = requireNonNull(level.getServer().getLevel(Level.OVERWORLD));
        return serverLevel.getDataStorage().computeIfAbsent(new SavedData.Factory<>(
            StorageRepositoryImpl::new,
            StorageRepositoryImpl::new,
            null
        ), StorageRepositoryImpl.NAME);
    }

    @Override
    public StorageContainerItemHelper getStorageContainerItemHelper() {
        return storageContainerItemHelper;
    }

    @Override
    public PlatformRegistry<ResourceType> getResourceTypeRegistry() {
        return resourceTypeRegistry;
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
    public void addStorageMonitorExtractionStrategy(final StorageMonitorExtractionStrategy strategy) {
        storageMonitorExtractionStrategy.addStrategy(strategy);
    }

    @Override
    public StorageMonitorExtractionStrategy getStorageMonitorExtractionStrategy() {
        return storageMonitorExtractionStrategy;
    }

    @Override
    public void addStorageMonitorInsertionStrategy(final StorageMonitorInsertionStrategy strategy) {
        storageMonitorInsertionStrategy.addStrategy(strategy);
    }

    @Override
    public StorageMonitorInsertionStrategy getStorageMonitorInsertionStrategy() {
        return storageMonitorInsertionStrategy;
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
    public InWorldNetworkNodeContainer createInWorldNetworkNodeContainer(
        final BlockEntity blockEntity,
        final NetworkNode node,
        final String name,
        final int priority,
        final ConnectionLogic connectionLogic,
        @Nullable final Supplier<Object> keyProvider
    ) {
        return new InWorldNetworkNodeContainerImpl(blockEntity, node, name, priority, connectionLogic, keyProvider);
    }

    @Override
    public void onNetworkNodeContainerInitialized(final InWorldNetworkNodeContainer container,
                                                  @Nullable final Level level,
                                                  @Nullable final Runnable callback) {
        if (level == null || level.isClientSide()) {
            return;
        }
        final ConnectionProviderImpl connectionProvider = new ConnectionProviderImpl(level);
        ServerEventQueue.queue(() -> {
            // The container could've been removed by the time it has been placed, and by the time the event queue has
            // run. In that case, don't initialize the network node because it no longer exists.
            // This is a workaround for the "Carry On" mod. The mod places the block (which creates a block entity and
            // requests this network node initialization) and then overrides the placed block entity with their own
            // block entity. This triggers a new initialization, but then this one can no longer run!
            if (container.isRemoved()) {
                return;
            }
            networkBuilder.initialize(container, connectionProvider);
            if (callback != null) {
                callback.run();
            }
        });
    }

    @Override
    public void onNetworkNodeContainerRemoved(final InWorldNetworkNodeContainer container,
                                              @Nullable final Level level) {
        if (level == null || level.isClientSide()) {
            return;
        }
        // "Carry On" mod places the block (which creates a block entity and requests network node initialization)
        // and then overrides the placed block entity with their own information.
        // However, when the placed block entity is replaced, the server event queue hasn't run yet and there is
        // no network loaded yet, even though the network node initialization was requested.
        // Stop continuing here to avoid further code failing due to a missing network.
        if (container.getNode().getNetwork() == null) {
            return;
        }
        final ConnectionProviderImpl connectionProvider = new ConnectionProviderImpl(level);
        networkBuilder.remove(container, connectionProvider);
    }

    @Override
    public void onNetworkNodeContainerUpdated(final InWorldNetworkNodeContainer container,
                                              @Nullable final Level level) {
        if (level == null || level.isClientSide() || container.getNode().getNetwork() == null) {
            return;
        }
        final ConnectionProviderImpl connectionProvider = new ConnectionProviderImpl(level);
        networkBuilder.update(container, connectionProvider);
    }

    @Override
    public GridInsertionStrategy createGridInsertionStrategy(final AbstractContainerMenu containerMenu,
                                                             final ServerPlayer player,
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
                                                               final ServerPlayer player,
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
                                                             final ServerPlayer player,
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
    public void addResourceFactory(final ResourceFactory factory) {
        resourceFactories.add(factory);
    }

    @Override
    public ResourceFactory getItemResourceFactory() {
        return itemResourceFactory;
    }

    @Override
    public StorageType getItemStorageType() {
        return StorageTypes.ITEM;
    }

    @Override
    public ResourceFactory getFluidResourceFactory() {
        return fluidResourceFactory;
    }

    @Override
    public StorageType getFluidStorageType() {
        return StorageTypes.FLUID;
    }

    @Override
    public Set<ResourceFactory> getAlternativeResourceFactories() {
        return resourceFactories;
    }

    @Override
    public <T extends ResourceKey> void registerResourceRendering(final Class<T> resourceClass,
                                                                  final ResourceRendering rendering) {
        resourceRenderingMap.put(resourceClass, rendering);
    }

    @Override
    public ResourceRendering getResourceRendering(final ResourceKey resource) {
        return resourceRenderingMap.get(resource.getClass());
    }

    @Override
    public void addIngredientConverter(final RecipeModIngredientConverter converter) {
        ingredientConverter.addConverter(converter);
    }

    @Override
    public RecipeModIngredientConverter getIngredientConverter() {
        return ingredientConverter;
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

    @Override
    public EnergyStorage asBlockItemEnergyStorage(final EnergyStorage energyStorage,
                                                  final ItemStack stack,
                                                  final BlockEntityType<?> blockEntityType) {
        return new ItemBlockEnergyStorage(energyStorage, stack, blockEntityType);
    }

    @Override
    public NetworkBoundItemHelper getNetworkBoundItemHelper() {
        return networkBoundItemHelper;
    }

    @Override
    public PlatformRegistry<SlotReferenceFactory> getSlotReferenceFactoryRegistry() {
        return slotReferenceFactoryRegistry;
    }

    @Override
    public void addSlotReferenceProvider(final SlotReferenceProvider provider) {
        slotReferenceProvider.addProvider(provider);
    }

    @Override
    public SlotReference createInventorySlotReference(final Player player, final InteractionHand hand) {
        return InventorySlotReference.of(player, hand);
    }

    @Override
    public void useNetworkBoundItem(final Player player, final Item... items) {
        final Set<Item> validItems = new HashSet<>(Arrays.asList(items));
        slotReferenceProvider.findForUse(player, items[0], validItems).ifPresent(C2SPackets::sendUseNetworkBoundItem);
    }

    @Override
    public BuiltinPermissions getBuiltinPermissions() {
        return BuiltinPermission.VIEW;
    }

    @Override
    public PlatformRegistry<PlatformPermission> getPermissionRegistry() {
        return permissionRegistry;
    }

    @Override
    public SecurityPolicy createDefaultSecurityPolicy() {
        return new SecurityPolicy(permissionRegistry.getAll()
            .stream()
            .filter(PlatformPermission::isAllowedByDefault)
            .collect(Collectors.toSet()));
    }

    @Override
    public void sendNoPermissionToOpenMessage(final ServerPlayer player, final Component target) {
        sendNoPermissionMessage(player, createTranslation("misc", "no_permission.open", target));
    }

    @Override
    public void sendNoPermissionMessage(final ServerPlayer player, final Component message) {
        S2CPackets.sendNoPermission(player, message);
    }

    @Override
    public boolean canPlaceNetworkNode(final ServerPlayer player,
                                       final Level level,
                                       final BlockPos pos,
                                       final BlockState state) {
        for (final Direction direction : Direction.values()) {
            final BlockPos adjacentPos = pos.relative(direction);
            final BlockEntity adjacentBlockEntity = level.getBlockEntity(adjacentPos);
            if (!(adjacentBlockEntity instanceof NetworkNodeContainerBlockEntity adjacentContainerBlockEntity)) {
                continue;
            }
            if (!adjacentContainerBlockEntity.canBuild(player)) {
                PlatformApi.INSTANCE.sendNoPermissionMessage(
                    player,
                    createTranslation("misc", "no_permission.build.place", state.getBlock().getName())
                );
                return false;
            }
        }
        return true;
    }
}
