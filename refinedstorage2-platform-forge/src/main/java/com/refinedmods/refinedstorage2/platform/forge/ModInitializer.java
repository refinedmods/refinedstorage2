package com.refinedmods.refinedstorage2.platform.forge;

import com.refinedmods.refinedstorage2.platform.api.network.ControllerType;
import com.refinedmods.refinedstorage2.platform.common.AbstractModInitializer;
import com.refinedmods.refinedstorage2.platform.common.block.BaseBlock;
import com.refinedmods.refinedstorage2.platform.common.block.CableBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ControllerBlock;
import com.refinedmods.refinedstorage2.platform.common.block.DiskDriveBlock;
import com.refinedmods.refinedstorage2.platform.common.block.FluidGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemGridBlock;
import com.refinedmods.refinedstorage2.platform.common.block.ItemStorageBlock;
import com.refinedmods.refinedstorage2.platform.common.block.MachineCasingBlock;
import com.refinedmods.refinedstorage2.platform.common.block.QuartzEnrichedIronBlock;
import com.refinedmods.refinedstorage2.platform.common.block.entity.CableBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.ControllerBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.FluidGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.grid.ItemGridBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.block.entity.storage.ItemStorageBlockEntity;
import com.refinedmods.refinedstorage2.platform.common.containermenu.ControllerContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.diskdrive.DiskDriveContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.FluidGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.ItemGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.containermenu.storage.ItemStorageContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.content.BlockEntities;
import com.refinedmods.refinedstorage2.platform.common.content.Blocks;
import com.refinedmods.refinedstorage2.platform.common.content.Items;
import com.refinedmods.refinedstorage2.platform.common.content.Menus;
import com.refinedmods.refinedstorage2.platform.common.content.Sounds;
import com.refinedmods.refinedstorage2.platform.common.internal.storage.type.ItemStorageType;
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
import com.refinedmods.refinedstorage2.platform.common.item.block.NameableBlockItem;
import com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil;
import com.refinedmods.refinedstorage2.platform.common.util.TickHandler;
import com.refinedmods.refinedstorage2.platform.forge.block.entity.ForgeDiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.forge.internal.PlatformImpl;
import com.refinedmods.refinedstorage2.platform.forge.packet.NetworkManager;

import net.minecraft.core.Direction;
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

    public ModInitializer() {
        initializePlatform(new PlatformImpl(new NetworkManager()));
        initializePlatformApiFacade();
        registerDiskTypes();
        registerStorageChannelTypes();
        registerNetworkComponents();
        registerResourceTypes();
        registerTickHandler();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onClientSetup);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(ClientModInitializer::onRegisterModels);
        });

        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Block.class, this::registerBlocks);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(BlockEntityType.class, this::registerBlockEntityTypes);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Item.class, this::registerItems);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(MenuType.class, this::registerMenus);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(SoundEvent.class, this::registerSounds);

        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::registerCapabilities);
    }

    private void registerTickHandler() {
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> e) {
        CableBlock cableBlock = new CableBlock();
        cableBlock.setRegistryName(CABLE);
        Blocks.INSTANCE.setCable(cableBlock);
        e.getRegistry().register(cableBlock);

        QuartzEnrichedIronBlock quartzEnrichedIronBlock = new QuartzEnrichedIronBlock();
        quartzEnrichedIronBlock.setRegistryName(QUARTZ_ENRICHED_IRON_BLOCK);
        Blocks.INSTANCE.setQuartzEnrichedIron(quartzEnrichedIronBlock);
        e.getRegistry().register(quartzEnrichedIronBlock);

        DiskDriveBlock diskDriveBlock = new DiskDriveBlock(ForgeDiskDriveBlockEntity::new);
        diskDriveBlock.setRegistryName(DISK_DRIVE);
        Blocks.INSTANCE.setDiskDrive(diskDriveBlock);
        e.getRegistry().register(diskDriveBlock);

        MachineCasingBlock machineCasingBlock = new MachineCasingBlock();
        machineCasingBlock.setRegistryName(MACHINE_CASING);
        Blocks.INSTANCE.setMachineCasing(machineCasingBlock);
        e.getRegistry().register(machineCasingBlock);

        Blocks.INSTANCE.getGrid().putAll(color -> {
            ItemGridBlock block = new ItemGridBlock(Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid")));
            block.setRegistryName(Blocks.INSTANCE.getGrid().getId(color, GRID));
            e.getRegistry().register(block);
            return block;
        });
        Blocks.INSTANCE.getFluidGrid().putAll(color -> {
            FluidGridBlock block = new FluidGridBlock(Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid")));
            block.setRegistryName(Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID));
            e.getRegistry().register(block);
            return block;
        });
        Blocks.INSTANCE.getController().putAll(color -> {
            ControllerBlock block = new ControllerBlock(ControllerType.NORMAL, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")));
            block.setRegistryName(Blocks.INSTANCE.getController().getId(color, CONTROLLER));
            e.getRegistry().register(block);
            return block;
        });
        Blocks.INSTANCE.getCreativeController().putAll(color -> {
            ControllerBlock block = new ControllerBlock(ControllerType.CREATIVE, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller")));
            block.setRegistryName(Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER));
            e.getRegistry().register(block);
            return block;
        });

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            ItemStorageBlock block = new ItemStorageBlock(variant);
            block.setRegistryName(forItemStorageBlock(variant));
            Blocks.INSTANCE.getItemStorageBlocks().put(variant, block);
            e.getRegistry().register(block);
        }
    }

    @SubscribeEvent
    public void registerBlockEntityTypes(RegistryEvent.Register<BlockEntityType<?>> e) {
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
            BlockEntityType<ItemStorageBlockEntity> blockEntityType = BlockEntityType.Builder.of((pos, state) -> new ItemStorageBlockEntity(pos, state, variant), Blocks.INSTANCE.getItemStorageBlocks().get(variant)).build(null);
            blockEntityType.setRegistryName(forItemStorageBlock(variant));
            e.getRegistry().register(blockEntityType);
            BlockEntities.INSTANCE.getItemStorageBlocks().put(variant, blockEntityType);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getCable(), createProperties()).setRegistryName(CABLE));
        e.getRegistry().register(new QuartzEnrichedIronItem(createProperties()).setRegistryName(QUARTZ_ENRICHED_IRON));
        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getQuartzEnrichedIron(), createProperties()).setRegistryName(QUARTZ_ENRICHED_IRON_BLOCK));
        e.getRegistry().register(new SiliconItem(createProperties()).setRegistryName(SILICON));
        e.getRegistry().register(new ProcessorBindingItem(createProperties()).setRegistryName(PROCESSOR_BINDING));
        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getDiskDrive(), createProperties()).setRegistryName(DISK_DRIVE));
        e.getRegistry().register(new WrenchItem(createProperties().stacksTo(1)).setRegistryName(WRENCH));

        StorageHousingItem storageHousingItem = new StorageHousingItem(createProperties());
        storageHousingItem.setRegistryName(STORAGE_HOUSING);
        e.getRegistry().register(storageHousingItem);
        Items.INSTANCE.setStorageHousing(storageHousingItem);

        e.getRegistry().register(new BlockItem(Blocks.INSTANCE.getMachineCasing(), createProperties()).setRegistryName(MACHINE_CASING));

        Blocks.INSTANCE.getGrid().forEach((color, block) -> e.getRegistry().register(new NameableBlockItem(block, createProperties(), color, Blocks.INSTANCE.getGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "grid"))).setRegistryName(Blocks.INSTANCE.getGrid().getId(color, GRID))));
        Blocks.INSTANCE.getFluidGrid().forEach((color, block) -> e.getRegistry().register(new NameableBlockItem(block, createProperties(), color, Blocks.INSTANCE.getFluidGrid().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "fluid_grid"))).setRegistryName(Blocks.INSTANCE.getFluidGrid().getId(color, FLUID_GRID))));
        Blocks.INSTANCE.getController().forEach((color, block) -> {
            ControllerBlockItem controllerBlockItem = new ControllerBlockItem(block, createProperties().stacksTo(1), color, Blocks.INSTANCE.getController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "controller")));
            controllerBlockItem.setRegistryName(Blocks.INSTANCE.getController().getId(color, CONTROLLER));
            Items.INSTANCE.getControllers().add(controllerBlockItem);
            e.getRegistry().register(controllerBlockItem);
        });
        Blocks.INSTANCE.getCreativeController().forEach((color, block) -> e.getRegistry().register(new NameableBlockItem(block, createProperties().stacksTo(1), color, Blocks.INSTANCE.getCreativeController().getName(color, createTranslation(BLOCK_TRANSLATION_CATEGORY, "creative_controller"))).setRegistryName(Blocks.INSTANCE.getCreativeController().getId(color, CREATIVE_CONTROLLER))));

        for (ProcessorItem.Type type : ProcessorItem.Type.values()) {
            e.getRegistry().register(new ProcessorItem(createProperties()).setRegistryName(forProcessor(type)));
        }

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            if (variant != ItemStorageType.Variant.CREATIVE) {
                StoragePartItem storagePartItem = new StoragePartItem(createProperties());
                storagePartItem.setRegistryName(forItemStoragePart(variant));
                e.getRegistry().register(storagePartItem);
                Items.INSTANCE.getStorageParts().put(variant, storagePartItem);
            }
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            if (type != FluidStorageDiskItem.FluidStorageType.CREATIVE) {
                FluidStoragePartItem fluidStoragePartItem = new FluidStoragePartItem(createProperties());
                fluidStoragePartItem.setRegistryName(forFluidStoragePart(type));
                e.getRegistry().register(fluidStoragePartItem);
                Items.INSTANCE.getFluidStorageParts().put(type, fluidStoragePartItem);
            }
        }

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            e.getRegistry().register(new ItemStorageDiskItem(createProperties().stacksTo(1).fireResistant(), variant).setRegistryName(forStorageDisk(variant)));
        }

        for (ItemStorageType.Variant variant : ItemStorageType.Variant.values()) {
            BlockItem storageBlockItem = new BlockItem(Blocks.INSTANCE.getItemStorageBlocks().get(variant), createProperties());
            storageBlockItem.setRegistryName(forItemStorageBlock(variant));
            e.getRegistry().register(storageBlockItem);
        }

        for (FluidStorageDiskItem.FluidStorageType type : FluidStorageDiskItem.FluidStorageType.values()) {
            e.getRegistry().register(new FluidStorageDiskItem(createProperties().stacksTo(1).fireResistant(), type).setRegistryName(forFluidStorageDisk(type)));
        }

        e.getRegistry().register(new CoreItem(createProperties()).setRegistryName(CONSTRUCTION_CORE));
        e.getRegistry().register(new CoreItem(createProperties()).setRegistryName(DESTRUCTION_CORE));
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

        MenuType<ItemStorageContainerMenu> storageMenuType = IForgeMenuType.create(ItemStorageContainerMenu::new);
        storageMenuType.setRegistryName(createIdentifier("storage"));
        e.getRegistry().register(storageMenuType);
        Menus.INSTANCE.setItemStorage(storageMenuType);
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
