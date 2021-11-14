package com.refinedmods.refinedstorage2.platform.fabric;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParser;
import com.refinedmods.refinedstorage2.api.grid.search.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.api.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.api.network.component.NetworkComponentRegistry;
import com.refinedmods.refinedstorage2.api.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.api.storage.channel.StorageChannelTypeRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacade;
import com.refinedmods.refinedstorage2.platform.fabric.api.Rs2PlatformApiFacadeProxy;
import com.refinedmods.refinedstorage2.platform.fabric.api.storage.type.StorageTypeRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.block.entity.diskdrive.DiskDriveBlockEntity;
import com.refinedmods.refinedstorage2.platform.fabric.init.Rs2BlockEntities;
import com.refinedmods.refinedstorage2.platform.fabric.init.Rs2Blocks;
import com.refinedmods.refinedstorage2.platform.fabric.init.Rs2Items;
import com.refinedmods.refinedstorage2.platform.fabric.init.Rs2ScreenHandlers;
import com.refinedmods.refinedstorage2.platform.fabric.integration.ReiIntegration;
import com.refinedmods.refinedstorage2.platform.fabric.internal.Rs2PlatformApiFacadeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.search.PlatformSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.view.GridResourceAttributeKeys;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.FluidStorageType;
import com.refinedmods.refinedstorage2.platform.fabric.internal.storage.type.ItemStorageType;
import com.refinedmods.refinedstorage2.platform.fabric.loot.Rs2LootFunctions;
import com.refinedmods.refinedstorage2.platform.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridInsertPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.platform.fabric.packet.c2s.StorageInfoRequestPacket;
import com.refinedmods.refinedstorage2.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.query.parser.ParserOperatorMappings;

import java.util.Set;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rs2Mod implements ModInitializer {
    public static final Rs2Blocks BLOCKS = new Rs2Blocks();
    public static final Rs2Items ITEMS = new Rs2Items();
    public static final Rs2BlockEntities BLOCK_ENTITIES = new Rs2BlockEntities();
    public static final Rs2ScreenHandlers SCREEN_HANDLERS = new Rs2ScreenHandlers();
    public static final Rs2LootFunctions LOOT_FUNCTIONS = new Rs2LootFunctions();
    static final String ID = "refinedstorage2";
    private static final Logger LOGGER = LogManager.getLogger(Rs2Mod.class);
    private static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(createIdentifier("general"), () -> new ItemStack(BLOCKS.getController().getNormal()));
    public static final Set<FeatureFlag> FEATURES = Set.of();

    private static SoundEvent wrenchSoundEvent;

    public static Identifier createIdentifier(String value) {
        return new Identifier(ID, value);
    }

    private static String createTranslationKey(String category, String value) {
        return String.format("%s.%s.%s", category, ID, value);
    }

    public static TranslatableText createTranslation(String category, String value, Object... args) {
        return new TranslatableText(createTranslationKey(category, value), args);
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(Rs2Config.class, Toml4jConfigSerializer::new);

        initializePlatformApiFacade();
        registerDiskTypes();
        registerStorageChannelTypes();
        registerNetworkComponents();
        registerContent();
        registerGridSearchBoxModes();
        registerPackets();
        registerSounds();
        registerInventories();

        LOGGER.info("Refined Storage 2 has loaded.");
    }

    private void registerInventories() {
        ItemStorage.SIDED.registerForBlockEntities((blockEntity, context) -> {
            if (blockEntity instanceof DiskDriveBlockEntity diskDrive) {
                return InventoryStorage.of(diskDrive.getDiskInventory(), context);
            }
            return null;
        }, BLOCK_ENTITIES.getDiskDrive());
    }

    private void registerSounds() {
        Identifier wrenchSoundEventId = Rs2Mod.createIdentifier("wrench");
        wrenchSoundEvent = Registry.register(Registry.SOUND_EVENT, wrenchSoundEventId, new SoundEvent(wrenchSoundEventId));
    }

    public static SoundEvent getWrenchSoundEvent() {
        return wrenchSoundEvent;
    }

    private void registerDiskTypes() {
        StorageTypeRegistry.INSTANCE.addType(createIdentifier("item_disk"), ItemStorageType.INSTANCE);
        StorageTypeRegistry.INSTANCE.addType(createIdentifier("fluid_disk"), FluidStorageType.INSTANCE);
    }

    private void initializePlatformApiFacade() {
        ((Rs2PlatformApiFacadeProxy) Rs2PlatformApiFacade.INSTANCE).setFacade(new Rs2PlatformApiFacadeImpl());
    }

    private void registerStorageChannelTypes() {
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.ITEM);
        StorageChannelTypeRegistry.INSTANCE.addType(StorageChannelTypes.FLUID);
    }

    private void registerNetworkComponents() {
        NetworkComponentRegistry.INSTANCE.addComponent(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        NetworkComponentRegistry.INSTANCE.addComponent(GraphNetworkComponent.class, GraphNetworkComponent::new);
        NetworkComponentRegistry.INSTANCE.addComponent(StorageNetworkComponent.class, network ->
                new StorageNetworkComponent(StorageChannelTypeRegistry.INSTANCE));
    }

    private void registerContent() {
        BLOCKS.register();
        ITEMS.register(BLOCKS, ITEM_GROUP);
        BLOCK_ENTITIES.register(BLOCKS);
        SCREEN_HANDLERS.register();
        LOOT_FUNCTIONS.register();
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_INFO_REQUEST, new StorageInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_INSERT, new GridInsertPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_EXTRACT, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_SCROLL, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.PROPERTY_CHANGE, new PropertyChangePacket());
    }

    private void registerGridSearchBoxModes() {
        GridQueryParser queryParser = new GridQueryParserImpl(
                LexerTokenMappings.DEFAULT_MAPPINGS,
                ParserOperatorMappings.DEFAULT_MAPPINGS,
                GridResourceAttributeKeys.UNARY_OPERATOR_TO_ATTRIBUTE_KEY_MAPPING
        );

        for (boolean autoSelected : new boolean[]{false, true}) {
            GridSearchBoxModeRegistry.INSTANCE.add(new PlatformSearchBoxModeImpl(
                    queryParser,
                    createIdentifier("textures/icons.png"),
                    autoSelected ? 16 : 0,
                    96,
                    createTranslation("gui", String.format("grid.search_box_mode.normal%s", autoSelected ? "_autoselected" : "")),
                    autoSelected
            ));
        }

        if (ReiIntegration.isLoaded()) {
            ReiIntegration.registerGridSearchBoxModes(queryParser);
        }
    }
}
