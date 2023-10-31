package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyBlockEntity;
import com.refinedmods.refinedstorage2.platform.api.support.energy.EnergyItem;
import com.refinedmods.refinedstorage2.platform.api.support.resource.FluidResource;
import com.refinedmods.refinedstorage2.platform.api.support.resource.ItemResource;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntityTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.CreativeModeTabItems;
import com.refinedmods.refinedstorage2.platform.common.content.DirectRegistryCallback;
import com.refinedmods.refinedstorage2.platform.common.content.MenuTypeFactory;
import com.refinedmods.refinedstorage2.platform.common.grid.WirelessGridItem;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfaceBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.iface.InterfacePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.common.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.common.storage.diskdrive.AbstractDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseBlock;
import com.refinedmods.refinedstorage2.platform.common.upgrade.RegulatorUpgradeItem;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.fabric.exporter.StorageExporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.fabric.grid.strategy.FluidGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.grid.strategy.FluidGridInsertionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.grid.strategy.ItemGridExtractionStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.grid.strategy.ItemGridScrollingStrategy;
import com.refinedmods.refinedstorage2.platform.fabric.importer.StorageImporterTransferStrategyFactory;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.CraftingGridClearPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.CraftingGridRecipeTransferPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceFilterSlotChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceSlotAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.ResourceSlotChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.SingleAmountChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.UseNetworkBoundItemPacket;
import com.refinedmods.refinedstorage2.platform.fabric.storage.diskdrive.FabricDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.storage.externalstorage.StoragePlatformExternalStorageProviderFactory;
import com.refinedmods.refinedstorage2.platform.fabric.support.energy.EnergyStorageAdapter;
import com.refinedmods.refinedstorage2.platform.fabric.support.networkbounditem.TrinketsSlotReferenceFactory;
import com.refinedmods.refinedstorage2.platform.fabric.support.networkbounditem.TrinketsSlotReferenceProvider;
import com.refinedmods.refinedstorage2.platform.fabric.support.resource.ResourceContainerFluidStorageAdapter;
import com.refinedmods.refinedstorage2.platform.fabric.support.resource.VariantUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ModInitializerImpl extends AbstractModInitializer implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModInitializerImpl.class);

    @Override
    public void onInitialize() {
        AutoConfig.register(ConfigImpl.class, Toml4jConfigSerializer::new);

        initializePlatform(new PlatformImpl());
        initializePlatformApi();
        registerAdditionalGridInsertionStrategyFactories();
        registerGridExtractionStrategyFactories();
        registerGridScrollingStrategyFactories();
        registerImporterTransferStrategyFactories();
        registerExporterTransferStrategyFactories();
        registerExternalStorageProviderFactories();
        registerContent();
        registerPackets();
        registerSounds(new DirectRegistryCallback<>(BuiltInRegistries.SOUND_EVENT));
        registerRecipeSerializers(new DirectRegistryCallback<>(BuiltInRegistries.RECIPE_SERIALIZER));
        registerSidedHandlers();
        registerTickHandler();
        registerSlotReferenceProviders();
        registerWrenchingEvent();

        LOGGER.info("Refined Storage 2 has loaded.");
    }

    private void registerAdditionalGridInsertionStrategyFactories() {
        PlatformApi.INSTANCE.addGridInsertionStrategyFactory(FluidGridInsertionStrategy::new);
    }

    private void registerGridExtractionStrategyFactories() {
        PlatformApi.INSTANCE.addGridExtractionStrategyFactory(ItemGridExtractionStrategy::new);
        PlatformApi.INSTANCE.addGridExtractionStrategyFactory(FluidGridExtractionStrategy::new);
    }

    private void registerGridScrollingStrategyFactories() {
        PlatformApi.INSTANCE.addGridScrollingStrategyFactory(ItemGridScrollingStrategy::new);
    }

    private void registerImporterTransferStrategyFactories() {
        PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new StorageImporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                StorageChannelTypes.ITEM,
                VariantUtil::ofItemVariant,
                VariantUtil::toItemVariant,
                1
            )
        );
        PlatformApi.INSTANCE.getImporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new StorageImporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                StorageChannelTypes.FLUID,
                VariantUtil::ofFluidVariant,
                VariantUtil::toFluidVariant,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExporterTransferStrategyFactories() {
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("item"),
            new StorageExporterTransferStrategyFactory<>(
                ItemStorage.SIDED,
                StorageChannelTypes.ITEM,
                resource -> resource instanceof ItemResource itemResource
                    ? Optional.of(itemResource)
                    : Optional.empty(),
                VariantUtil::toItemVariant,
                1
            )
        );
        PlatformApi.INSTANCE.getExporterTransferStrategyRegistry().register(
            createIdentifier("fluid"),
            new StorageExporterTransferStrategyFactory<>(
                FluidStorage.SIDED,
                StorageChannelTypes.FLUID,
                resource -> resource instanceof FluidResource fluidResource
                    ? Optional.of(fluidResource)
                    : Optional.empty(),
                VariantUtil::toFluidVariant,
                FluidConstants.BUCKET
            )
        );
    }

    private void registerExternalStorageProviderFactories() {
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(new InterfacePlatformExternalStorageProviderFactory());
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(new StoragePlatformExternalStorageProviderFactory<>(
            StorageChannelTypes.ITEM,
            ItemStorage.SIDED,
            VariantUtil::ofItemVariant,
            VariantUtil::toItemVariant
        ));
        PlatformApi.INSTANCE.addExternalStorageProviderFactory(new StoragePlatformExternalStorageProviderFactory<>(
            StorageChannelTypes.FLUID,
            FluidStorage.SIDED,
            VariantUtil::ofFluidVariant,
            VariantUtil::toFluidVariant
        ));
    }

    private void registerContent() {
        registerBlocks(new DirectRegistryCallback<>(BuiltInRegistries.BLOCK), FabricDiskDriveBlockEntity::new);
        registerItems(
            new DirectRegistryCallback<>(BuiltInRegistries.ITEM),
            () -> new RegulatorUpgradeItem(PlatformApi.INSTANCE.getUpgradeRegistry()) {
                @Override
                public boolean allowNbtUpdateAnimation(final Player player,
                                                       final InteractionHand hand,
                                                       final ItemStack oldStack,
                                                       final ItemStack newStack) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            },
            () -> new WirelessGridItem(false) {
                @Override
                public boolean allowNbtUpdateAnimation(final Player player,
                                                       final InteractionHand hand,
                                                       final ItemStack oldStack,
                                                       final ItemStack newStack) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            },
            () -> new WirelessGridItem(true) {
                @Override
                public boolean allowNbtUpdateAnimation(final Player player,
                                                       final InteractionHand hand,
                                                       final ItemStack oldStack,
                                                       final ItemStack newStack) {
                    return AbstractModInitializer.allowNbtUpdateAnimation(oldStack, newStack);
                }
            }
        );
        registerUpgradeMappings();
        registerCreativeModeTab();
        registerBlockEntities(
            new DirectRegistryCallback<>(BuiltInRegistries.BLOCK_ENTITY_TYPE),
            new BlockEntityTypeFactory() {
                @Override
                public <T extends BlockEntity> BlockEntityType<T> create(final BlockEntitySupplier<T> factory,
                                                                         final Block... allowedBlocks) {
                    return new BlockEntityType<>(factory::create, new HashSet<>(Arrays.asList(allowedBlocks)), null);
                }
            },
            FabricDiskDriveBlockEntity::new
        );
        registerMenus(new DirectRegistryCallback<>(BuiltInRegistries.MENU), new MenuTypeFactory() {
            @Override
            public <T extends AbstractContainerMenu> MenuType<T> create(final MenuSupplier<T> supplier) {
                return new ExtendedScreenHandlerType<>(supplier::create);
            }
        });
        registerLootFunctions(new DirectRegistryCallback<>(BuiltInRegistries.LOOT_FUNCTION_TYPE));
    }

    private void registerCreativeModeTab() {
        Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            createIdentifier("general"),
            CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(createTranslation("itemGroup", "general"))
                .icon(() -> new ItemStack(Blocks.INSTANCE.getCreativeController().getDefault()))
                .displayItems((params, output) -> CreativeModeTabItems.append(output::accept))
                .build()
        );
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_REQUEST, new StorageInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_INSERT, new GridInsertPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_EXTRACT, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_SCROLL, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.CRAFTING_GRID_CLEAR, new CraftingGridClearPacket());
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.CRAFTING_GRID_RECIPE_TRANSFER,
            new CraftingGridRecipeTransferPacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.PROPERTY_CHANGE, new PropertyChangePacket());
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.RESOURCE_SLOT_AMOUNT_CHANGE,
            new ResourceSlotAmountChangePacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.RESOURCE_FILTER_SLOT_CHANGE,
            new ResourceFilterSlotChangePacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(
            PacketIds.RESOURCE_SLOT_CHANGE,
            new ResourceSlotChangePacket()
        );
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.SINGLE_AMOUNT_CHANGE, new SingleAmountChangePacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.USE_NETWORK_BOUND_ITEM, new UseNetworkBoundItemPacket());
    }

    @SuppressWarnings("checkstyle:Indentation")
    private void registerSidedHandlers() {
        registerItemStorage(
            AbstractDiskDriveBlockEntity.class::isInstance,
            AbstractDiskDriveBlockEntity.class::cast,
            AbstractDiskDriveBlockEntity::getDiskInventory,
            BlockEntities.INSTANCE.getDiskDrive()
        );
        registerItemStorage(
            InterfaceBlockEntity.class::isInstance,
            InterfaceBlockEntity.class::cast,
            InterfaceBlockEntity::getExportedResourcesAsContainer,
            BlockEntities.INSTANCE.getInterface()
        );
        FluidStorage.SIDED.registerForBlockEntity(
            (blockEntity, context) -> new ResourceContainerFluidStorageAdapter(blockEntity.getExportedResources()),
            BlockEntities.INSTANCE.getInterface()
        );
        registerEnergyBlockEntityProviders();
        registerEnergyItemProviders();
    }

    private <T extends BlockEntity> void registerItemStorage(final Predicate<BlockEntity> test,
                                                             final Function<BlockEntity, T> caster,
                                                             final Function<T, Container> containerSupplier,
                                                             final BlockEntityType<?> type) {
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (test.test(blockEntity)) {
                final T casted = caster.apply(blockEntity);
                return InventoryStorage.of(containerSupplier.apply(casted), context);
            }
            return null;
        }, type);
    }

    private void registerEnergyBlockEntityProviders() {
        EnergyStorage.SIDED.registerFallback(new BlockApiLookup.BlockApiProvider<>() {
            @Override
            @Nullable
            public EnergyStorage find(final Level world,
                                      final BlockPos pos,
                                      final BlockState state,
                                      @Nullable final BlockEntity blockEntity,
                                      final Direction context) {
                if (blockEntity instanceof EnergyBlockEntity energyBlockEntity) {
                    return new EnergyStorageAdapter(energyBlockEntity.getEnergyStorage());
                }
                return null;
            }
        });
    }

    private void registerEnergyItemProviders() {
        EnergyStorage.ITEM.registerFallback(new ItemApiLookup.ItemApiProvider<>() {
            @Override
            @Nullable
            public EnergyStorage find(final ItemStack itemStack, final ContainerItemContext context) {
                if (itemStack.getItem() instanceof EnergyItem energyItem) {
                    return energyItem.createEnergyStorage(itemStack).map(EnergyStorageAdapter::new).orElse(null);
                }
                return null;
            }
        });
    }

    private void registerTickHandler() {
        ServerTickEvents.START_SERVER_TICK.register(server -> TickHandler.runQueuedActions());
    }

    private void registerSlotReferenceProviders() {
        TrinketsSlotReferenceProvider.create().ifPresent(slotReferenceProvider -> {
            PlatformApi.INSTANCE.getSlotReferenceFactoryRegistry().register(
                createIdentifier("trinkets"),
                TrinketsSlotReferenceFactory.INSTANCE
            );
            PlatformApi.INSTANCE.addSlotReferenceProvider(slotReferenceProvider);
        });
    }

    private void registerWrenchingEvent() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            final BlockState state = level.getBlockState(hitResult.getBlockPos());
            return AbstractBaseBlock.tryUseWrench(state, level, hitResult, player, hand)
                .or(() -> AbstractBaseBlock.tryUpdateColor(state, level, hitResult.getBlockPos(), player, hand))
                .orElse(InteractionResult.PASS);
        });
    }
}
