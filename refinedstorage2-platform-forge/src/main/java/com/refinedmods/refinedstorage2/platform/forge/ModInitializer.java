package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.apiimpl.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.platform.common.block.QuartzEnrichedIronBlock;
import com.refinedmods.refinedstorage2.platform.common.block.StorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.FluidStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.FluidStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.block.ItemStorageBlockContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.LootFunctions;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.item.CoreItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.FluidStoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.ItemStorageDiskItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorBindingItem;
import com.refinedmods.refinedstorage2.platform.common.item.ProcessorItem;
import com.refinedmods.refinedstorage2.platform.common.item.QuartzEnrichedIronItem;
import com.refinedmods.refinedstorage2.platform.common.item.SiliconItem;
import com.refinedmods.refinedstorage2.platform.common.item.StorageHousingItem;
import com.refinedmods.refinedstorage2.platform.common.item.StoragePartItem;
import com.refinedmods.refinedstorage2.platform.common.item.WrenchItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ControllerBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.FluidStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.ItemStorageBlockBlockItem;
import com.refinedmods.refinedstorage2.platform.common.item.block.NameableBlockItem;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.forge.block.entity.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CABLE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONSTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.CREATIVE_CONTROLLER;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DESTRUCTION_CORE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.DISK_DRIVE;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.FLUID_GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.GRID;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.MACHINE_CASING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.PROCESSOR_BINDING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.QUARTZ_ENRICHED_IRON;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.QUARTZ_ENRICHED_IRON_BLOCK;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.SILICON;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.STORAGE_HOUSING;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.WRENCH;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStorageBlock;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStorageDisk;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forFluidStoragePart;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forItemStorageBlock;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forItemStoragePart;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forProcessor;
import static com.refinedmods.refinedstorage2.platform.common.content.ContentIds.forStorageDisk;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

@Mod(IdentifierUtil.MOD_ID)
public class ModInitializer extends AbstractModInitializer {
    private static final String BLOCK_TRANSLATION_CATEGORY = "block";
    private static final CreativeModeTab CREATIVE_MODE_TAB = new CreativeModeTab(IdentifierUtil.MOD_ID + ".general") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.INSTANCE.getController().getNormal());
        }
    };

    private final DeferredRegister<Block> blockRegistry = DeferredRegister.create(ForgeRegistries.BLOCKS, IdentifierUtil.MOD_ID);
    private final DeferredRegister<Item> itemRegistry = DeferredRegister.create(ForgeRegistries.ITEMS, IdentifierUtil.MOD_ID);

    public ModInitializer() {
        initializePlatform(new PlatformImpl(new NetworkManager()));
        initializePlatformApi();
        registerDiskTypes();
        registerStorageChannelTypes();
        registerNetworkComponents();
        registerContent();
        registerAdditionalResourceTypes();
        registerTickHandler();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onClientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onRegisterModels);
        });

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, this::registerBlockEntityTypes);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, this::registerMenus);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, this::registerSounds);

        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::registerCapabilities);
    }

    private void registerContent() {
        registerBlocks();
        registerItems();
    }

    private void registerLootFunctions() {
        LootFunctions.INSTANCE.setStorageBlock(Registry.register(Registry.LOOT_FUNCTION_TYPE, createIdentifier("storage_block"), new LootItemFunctionType(new StorageBlock.StorageBlockLootItemFunctionSerializer())));
    }

    private void registerBlocks() {
        Blocks.INSTANCE.setCable(blockRegistry.register(CABLE.getPath(), CableBlock::new));
        Blocks.INSTANCE.setQuartzEnrichedIronBlock(blockRegistry.register(QUARTZ_ENRICHED_IRON_BLOCK.getPath(), QuartzEnrichedIronBlock::new));
        Blocks.INSTANCE.setDiskDrive(blockRegistry.register(DISK_DRIVE.getPath(), () -> new DiskDriveBlock(ForgeDiskDriveBlockEntity::new)));
        Blocks.INSTANCE.setMachineCasing(blockRegistry.register(MACHINE_CASING.getPath(), MachineCasingBlock::new));
        Blocks.INSTANCE.getGrid().putAll(color -> blockRegistry.register(Blocks.INSTANCE.getGrid().getId(color, GRID).getPath(), () -> new ItemGridBlock(Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")))));
        Blocks.INSTANCE.getFluidGrid().putAll(color -> blockRegistry.register(Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID).getPath(), () -> new FluidGridBlock(Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")))));
        Blocks.INSTANCE.getController().putAll(color -> blockRegistry.register(Blocks.INSTANCE.getController().getId(color, CONTROLLER).getPath(), () -> new ControllerBlock(ControllerType.NORMAL, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")))));
        Blocks.INSTANCE.getCreativeController().putAll(color -> blockRegistry.register(Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER).getPath(), () -> new ControllerBlock(ControllerType.CREATIVE, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")))));

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            Blocks.INSTANCE.getItemStorageBlocks().put(variant, blockRegistry.register(forItemStorageBlock(variant).getPath(), () -> new ItemStorageBlock(variant)));
        }

        for (FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            Blocks.INSTANCE.getFluidStorageBlocks().put(variant, blockRegistry.register(forFluidStorageBlock(variant).getPath(), () -> new FluidStorageBlock(variant)));
        }

        blockRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void registerBlockEntityTypes(RegistryEvent.Register<BlockEntityType<?>> e) {
        // Register here, there seems to be no specific register event for loot function types.
        registerLootFunctions();

        BlockEntityType<CableBlockEntity> cableBlockEntityType = BlockEntityType.Builder.of(CableBlockEntity::new, Blocks.INSTANCE.getCable()).build(null);
        cableBlockEntityType.setRegistryName(CABLE);
        e.getRegistry().register(cableBlockEntityType);
        BlockEntities.INSTANCE.setCable(cableBlockEntityType);

        BlockEntityType<ControllerBlockEntity> controllerBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new ControllerBlockEntity(ControllerType.NORMAL, pos, state), Blocks.INSTANCE.getController().toArray()).build(null);
        controllerBlockEntityType.setRegistryName(CONTROLLER);
        e.getRegistry().register(controllerBlockEntityType);
        BlockEntities.INSTANCE.setController(controllerBlockEntityType);

        BlockEntityType<ControllerBlockEntity> creativeControllerBlockEntityType = BlockEntityType.Builder.of((pos, state) -> new ControllerBlockEntity(ControllerType.CREATIVE, pos, state), Blocks.INSTANCE.getCreativeController().toArray()).build(null);
        creativeControllerBlockEntityType.setRegistryName(CREATIVE_CONTROLLER);
        e.getRegistry().register(creativeControllerBlockEntityType);
        BlockEntities.INSTANCE.setCreativeController(creativeControllerBlockEntityType);

        BlockEntityType<ForgeDiskDriveBlockEntity> diskDriveBlockEntityType = BlockEntityType.Builder.of(ForgeDiskDriveBlockEntity::new, Blocks.INSTANCE.getDiskDrive()).build(null);
        diskDriveBlockEntityType.setRegistryName(DISK_DRIVE);
        e.getRegistry().register(diskDriveBlockEntityType);
        BlockEntities.INSTANCE.setDiskDrive(diskDriveBlockEntityType);

        BlockEntityType<ItemGridBlockEntity> gridBlockEntityType = BlockEntityType.Builder.of(ItemGridBlockEntity::new, Blocks.INSTANCE.getGrid().toArray()).build(null);
        gridBlockEntityType.setRegistryName(GRID);
        e.getRegistry().register(gridBlockEntityType);
        BlockEntities.INSTANCE.setGrid(gridBlockEntityType);

        BlockEntityType<FluidGridBlockEntity> fluidGridBlockEntityType = BlockEntityType.Builder.of(FluidGridBlockEntity::new, Blocks.INSTANCE.getFluidGrid().toArray()).build(null);
        fluidGridBlockEntityType.setRegistryName(FLUID_GRID);
        e.getRegistry().register(fluidGridBlockEntityType);
        BlockEntities.INSTANCE.setFluidGrid(fluidGridBlockEntityType);

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            BlockEntityType<ItemStorageBlockBlockEntity> blockEntityType = BlockEntityType.Builder.of((pos, state) -> new ItemStorageBlockBlockEntity(pos, state, variant), Blocks.INSTANCE.getItemStorageBlocks().get(variant).get()).build(null);
            blockEntityType.setRegistryName(forItemStorageBlock(variant));
            e.getRegistry().register(blockEntityType);
            BlockEntities.INSTANCE.getItemStorageBlocks().put(variant, blockEntityType);
        }

        for (FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            BlockEntityType<FluidStorageBlockBlockEntity> blockEntityType = BlockEntityType.Builder.of((pos, state) -> new FluidStorageBlockBlockEntity(pos, state, variant), Blocks.INSTANCE.getFluidStorageBlocks().get(variant).get()).build(null);
            blockEntityType.setRegistryName(forFluidStorageBlock(variant));
            e.getRegistry().register(blockEntityType);
            BlockEntities.INSTANCE.getFluidStorageBlocks().put(variant, blockEntityType);
        }
    }

    private void registerItems() {
        itemRegistry.register(CABLE.getPath(), () -> new BlockItem(Blocks.INSTANCE.getCable(), createProperties()));
        itemRegistry.register(QUARTZ_ENRICHED_IRON.getPath(), () -> new QuartzEnrichedIronItem(createProperties()));
        itemRegistry.register(QUARTZ_ENRICHED_IRON_BLOCK.getPath(), () -> new BlockItem(Blocks.INSTANCE.getQuartzEnrichedIronBlock(), createProperties()));
        itemRegistry.register(SILICON.getPath(), () -> new SiliconItem(createProperties()));
        itemRegistry.register(PROCESSOR_BINDING.getPath(), () -> new ProcessorBindingItem(createProperties()));
        itemRegistry.register(DISK_DRIVE.getPath(), () -> new BlockItem(Blocks.INSTANCE.getDiskDrive(), createProperties()));
        itemRegistry.register(WRENCH.getPath(), () -> new WrenchItem(createProperties().stacksTo(1)));
        Items.INSTANCE.setStorageHousing(itemRegistry.register(STORAGE_HOUSING.getPath(), () -> new StorageHousingItem(createProperties())));
        itemRegistry.register(MACHINE_CASING.getPath(), () -> new BlockItem(Blocks.INSTANCE.getMachineCasing(), createProperties()));
        Blocks.INSTANCE.getGrid().forEach((color, block) -> itemRegistry.register(Blocks.INSTANCE.getGrid().getId(color, GRID).getPath(), () -> new NameableBlockItem(block.get(), createProperties(), color, Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")))));
        Blocks.INSTANCE.getFluidGrid().forEach((color, block) -> itemRegistry.register(Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID).getPath(), () -> new NameableBlockItem(block.get(), createProperties(), color, Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")))));
        Blocks.INSTANCE.getController().forEach((color, block) -> Items.INSTANCE.getControllers().add(itemRegistry.register(
                Blocks.INSTANCE.getController().getId(color, CONTROLLER).getPath(),
                () -> new ControllerBlockItem(
                        block.get(),
                        createProperties().stacksTo(1),
                        color,
                        Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller"))
                )
        )));
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> itemRegistry.register(
                Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER).getPath(),
                () -> new NameableBlockItem(
                        block.get(),
                        createProperties().stacksTo(1),
                        color,
                        Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller"))
                )
        ));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            itemRegistry.register(forProcessor(type).getPath(), () -> new ProcessorItem(createProperties()));
        }

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            if (variant != ItemStorageType.Variant.CREATIVE) {
                Items.INSTANCE.getStorageParts().put(variant, itemRegistry.register(forItemStoragePart(variant).getPath(), () -> new StoragePartItem(createProperties())));
            }
        }

        for (FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            if (variant != FluidStorageType.Variant.CREATIVE) {
                Items.INSTANCE.getFluidStorageParts().put(variant, itemRegistry.register(forFluidStoragePart(variant).getPath(), () -> new FluidStoragePartItem(createProperties())));
            }
        }

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            itemRegistry.register(forStorageDisk(variant).getPath(), () -> new ItemStorageDiskItem(createProperties().stacksTo(1).fireResistant(), variant));
        }

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            itemRegistry.register(forItemStorageBlock(variant).getPath(), () -> new ItemStorageBlockBlockItem(Blocks.INSTANCE.getItemStorageBlocks().get(variant).get(), createProperties().stacksTo(1).fireResistant(), variant));
        }

        for (FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            itemRegistry.register(forFluidStorageDisk(variant).getPath(), () -> new FluidStorageDiskItem(createProperties().stacksTo(1).fireResistant(), variant));
        }

        for (FluidStorageType.Variant variant : FluidStorageType.Variant.values()) {
            itemRegistry.register(forFluidStorageBlock(variant).getPath(), () -> new FluidStorageBlockBlockItem(Blocks.INSTANCE.getFluidStorageBlocks().get(variant).get(), createProperties().stacksTo(1).fireResistant(), variant));
        }

        itemRegistry.register(CONSTRUCTION_CORE.getPath(), () -> new CoreItem(createProperties()));
        itemRegistry.register(DESTRUCTION_CORE.getPath(), () -> new CoreItem(createProperties()));

        itemRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private void registerTickHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        BlockState state = e.getWorld().getBlockState(e.getHitVec().getBlockPos());

        BaseBlock.tryUseWrench(state, e.getWorld(), e.getHitVec(), e.getPlayer(), e.getHand())
                .or(() -> BaseBlock.tryUpdateColor(state, e.getWorld(), e.getHitVec().getBlockPos(), e.getPlayer(), e.getHand()))
                .ifPresent(result -> {
                    e.setCanceled(true);
                    e.setCancellationResult(result);
                });
    }

    @SubscribeEvent
    public void registerCapabilities(AttachCapabilitiesEvent<BlockEntity> e) {
        if (e.getObject() instanceof ControllerBlockEntity controllerBlockEntity) {
            registerControllerEnergy(e, controllerBlockEntity);
        }
    }

    private void registerControllerEnergy(AttachCapabilitiesEvent<BlockEntity> e, ControllerBlockEntity controllerBlockEntity) {
        LazyOptional<IEnergyStorage> capability = LazyOptional.of(() -> (IEnergyStorage) controllerBlockEntity.getEnergyStorage());
        e.addCapability(createIdentifier("energy"), new ICapabilityProvider() {
            @NotNull
            @Override
            public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
                if (cap == CapabilityEnergy.ENERGY && controllerBlockEntity.getEnergyStorage() instanceof IEnergyStorage) {
                    return capability.cast();
                }
                return LazyOptional.empty();
            }
        });
    }

    // TODO: Delegate this responsibility to the items themselves..
    private Item.Properties createProperties() {
        return new Item.Properties().tab(CREATIVE_MODE_TAB);
    }

    @SubscribeEvent
    public void registerMenus(RegistryEvent.Register<MenuType<?>> e) {
        MenuType<ControllerContainerMenu> controllerMenuType = IForgeMenuType.create(ControllerContainerMenu::new);
        controllerMenuType.setRegistryName(CONTROLLER);
        e.getRegistry().register(controllerMenuType);
        Menus.INSTANCE.setController(controllerMenuType);

        MenuType<DiskDriveContainerMenu> diskDriveMenuType = IForgeMenuType.create(DiskDriveContainerMenu::new);
        diskDriveMenuType.setRegistryName(DISK_DRIVE);
        e.getRegistry().register(diskDriveMenuType);
        Menus.INSTANCE.setDiskDrive(diskDriveMenuType);

        MenuType<ItemGridContainerMenu> itemGridMenuType = IForgeMenuType.create(ItemGridContainerMenu::new);
        itemGridMenuType.setRegistryName(GRID);
        e.getRegistry().register(itemGridMenuType);
        Menus.INSTANCE.setGrid(itemGridMenuType);

        MenuType<FluidGridContainerMenu> fluidGridMenuType = IForgeMenuType.create(FluidGridContainerMenu::new);
        fluidGridMenuType.setRegistryName(FLUID_GRID);
        e.getRegistry().register(fluidGridMenuType);
        Menus.INSTANCE.setFluidGrid(fluidGridMenuType);

        MenuType<ItemStorageBlockContainerMenu> itemStorageMenuType = IForgeMenuType.create(ItemStorageBlockContainerMenu::new);
        itemStorageMenuType.setRegistryName(createIdentifier("item_storage"));
        e.getRegistry().register(itemStorageMenuType);
        Menus.INSTANCE.setItemStorage(itemStorageMenuType);

        MenuType<FluidStorageBlockContainerMenu> fluidStorageMenuType = IForgeMenuType.create(FluidStorageBlockContainerMenu::new);
        fluidStorageMenuType.setRegistryName(createIdentifier("fluid_storage"));
        e.getRegistry().register(fluidStorageMenuType);
        Menus.INSTANCE.setFluidStorage(fluidStorageMenuType);
    }

    @SubscribeEvent
    public void registerSounds(RegistryEvent.Register<SoundEvent> e) {
        SoundEvent wrenchSoundEvent = new SoundEvent(WRENCH);
        wrenchSoundEvent.setRegistryName(WRENCH);
        e.getRegistry().register(wrenchSoundEvent);
        Sounds.INSTANCE.setWrench(wrenchSoundEvent);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            TickHandler.runQueuedActions();
        }
    }
}
