package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.core.Rs2CoreApiFacade;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeDisplayProperties;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.core.network.component.EnergyNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.GraphNetworkComponent;
import com.refinedmods.refinedstorage2.core.network.component.StorageNetworkComponent;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.core.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.core.storage.channel.StorageChannelTypes;
import com.refinedmods.refinedstorage2.fabric.coreimpl.FabricRs2ApiFacade;
import com.refinedmods.refinedstorage2.fabric.init.Rs2BlockEntities;
import com.refinedmods.refinedstorage2.fabric.init.Rs2Blocks;
import com.refinedmods.refinedstorage2.fabric.init.Rs2Items;
import com.refinedmods.refinedstorage2.fabric.init.Rs2ScreenHandlers;
import com.refinedmods.refinedstorage2.fabric.integration.ReiIntegration;
import com.refinedmods.refinedstorage2.fabric.loot.Rs2LootFunctions;
import com.refinedmods.refinedstorage2.fabric.packet.PacketIds;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.StorageDiskInfoRequestPacket;

import java.util.Set;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rs2Mod implements ModInitializer {
    public static final Rs2CoreApiFacade<World> API = new FabricRs2ApiFacade();
    public static final Rs2Blocks BLOCKS = new Rs2Blocks();
    public static final Rs2Items ITEMS = new Rs2Items();
    public static final Rs2BlockEntities BLOCK_ENTITIES = new Rs2BlockEntities();
    public static final Rs2ScreenHandlers SCREEN_HANDLERS = new Rs2ScreenHandlers();
    public static final Rs2LootFunctions LOOT_FUNCTIONS = new Rs2LootFunctions();
    static final String ID = "refinedstorage2";
    private static final Logger LOGGER = LogManager.getLogger(Rs2Mod.class);
    private static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(createIdentifier("general"), () -> new ItemStack(BLOCKS.getController().getNormal()));
    public static final Set<FeatureFlag> FEATURES = Set.of();

    public static Identifier createIdentifier(String value) {
        return new Identifier(ID, value);
    }

    public static String createTranslationKey(String category, String value) {
        return String.format("%s.%s.%s", category, ID, value);
    }

    public static TranslatableText createTranslation(String category, String value, Object... args) {
        return new TranslatableText(createTranslationKey(category, value), args);
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(Rs2Config.class, Toml4jConfigSerializer::new);

        registerStorageChannelTypes();
        registerNetworkComponents();
        registerContent();
        registerGridSearchBoxModes();
        registerPackets();

        LOGGER.info("Refined Storage 2 has loaded.");
    }

    private void registerStorageChannelTypes() {
        Rs2Mod.API.getStorageChannelTypeRegistry().addType(StorageChannelTypes.ITEM);
    }

    private void registerNetworkComponents() {
        Rs2Mod.API.getNetworkComponentRegistry().addComponent(EnergyNetworkComponent.class, network -> new EnergyNetworkComponent());
        Rs2Mod.API.getNetworkComponentRegistry().addComponent(GraphNetworkComponent.class, GraphNetworkComponent::new);
        Rs2Mod.API.getNetworkComponentRegistry().addComponent(StorageNetworkComponent.class, network -> new StorageNetworkComponent(Rs2Mod.API.getStorageChannelTypeRegistry()));
    }

    private void registerContent() {
        BLOCKS.register();
        ITEMS.register(BLOCKS, ITEM_GROUP);
        BLOCK_ENTITIES.register(BLOCKS);
        SCREEN_HANDLERS.register();
        LOOT_FUNCTIONS.register();
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.STORAGE_DISK_INFO_REQUEST, new StorageDiskInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_INSERT_FROM_CURSOR, new GridInsertFromCursorPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_EXTRACT, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.GRID_SCROLL, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PacketIds.PROPERTY_CHANGE, new PropertyChangePacket());
    }

    private void registerGridSearchBoxModes() {
        GridQueryParser queryParser = new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS);

        for (boolean autoSelected : new boolean[]{false, true}) {
            API.getGridSearchBoxModeRegistry().add(new GridSearchBoxModeImpl(queryParser, autoSelected, createSearchBoxModeDisplayProperties(autoSelected)));
        }

        if (ReiIntegration.isLoaded()) {
            ReiIntegration.registerGridSearchBoxModes(queryParser);
        }
    }

    private GridSearchBoxModeDisplayProperties createSearchBoxModeDisplayProperties(boolean autoSelected) {
        return new GridSearchBoxModeDisplayProperties(
                createIdentifier("textures/icons.png").toString(),
                autoSelected ? 16 : 0,
                96,
                createTranslationKey("gui", String.format("grid.search_box_mode.normal%s", autoSelected ? "_autoselected" : ""))
        );
    }
}
