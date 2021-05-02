package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.core.Rs2ApiFacade;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeDisplayProperties;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.core.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.fabric.coreimpl.FabricRs2ApiFacade;
import com.refinedmods.refinedstorage2.fabric.coreimpl.grid.ReiGridSearchBoxMode;
import com.refinedmods.refinedstorage2.fabric.init.Rs2BlockEntities;
import com.refinedmods.refinedstorage2.fabric.init.Rs2Blocks;
import com.refinedmods.refinedstorage2.fabric.init.Rs2Items;
import com.refinedmods.refinedstorage2.fabric.init.Rs2ScreenHandlers;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.StorageDiskInfoRequestPacket;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Rs2Mod implements ModInitializer {
    public static final Rs2ApiFacade<MinecraftServer, World> API = new FabricRs2ApiFacade();
    public static final Rs2Blocks BLOCKS = new Rs2Blocks();
    public static final Rs2Items ITEMS = new Rs2Items();
    public static final Rs2BlockEntities BLOCK_ENTITIES = new Rs2BlockEntities();
    public static final Rs2ScreenHandlers SCREEN_HANDLERS = new Rs2ScreenHandlers();
    static final String ID = "refinedstorage2";
    private static final Logger LOGGER = LogManager.getLogger(Rs2Mod.class);
    private static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(createIdentifier("general"), () -> new ItemStack(BLOCKS.getController().getNormal()));

    public static Identifier createIdentifier(String value) {
        return new Identifier(ID, value);
    }

    public static boolean isModIdentifier(Identifier identifier) {
        return ID.equals(identifier.getNamespace());
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

        registerContent();
        registerGridSearchBoxModes();
        registerPackets();

        LOGGER.info("Refined Storage 2 has loaded.");
    }

    private void registerContent() {
        BLOCKS.register();
        ITEMS.register(BLOCKS, ITEM_GROUP);
        BLOCK_ENTITIES.register(BLOCKS);
        SCREEN_HANDLERS.register();
    }

    private void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(StorageDiskInfoRequestPacket.ID, new StorageDiskInfoRequestPacket());
        ServerPlayNetworking.registerGlobalReceiver(GridInsertFromCursorPacket.ID, new GridInsertFromCursorPacket());
        ServerPlayNetworking.registerGlobalReceiver(GridExtractPacket.ID, new GridExtractPacket());
        ServerPlayNetworking.registerGlobalReceiver(GridScrollPacket.ID, new GridScrollPacket());
        ServerPlayNetworking.registerGlobalReceiver(PropertyChangePacket.ID, new PropertyChangePacket());
    }

    private void registerGridSearchBoxModes() {
        GridQueryParser queryParser = new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS);

        for (boolean autoSelected : new boolean[]{false, true}) {
            API.getGridSearchBoxModeRegistry().add(new GridSearchBoxModeImpl(queryParser, autoSelected, createSearchBoxModeDisplayProperties(autoSelected)));
        }

        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, false, false)); // REI
        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, true, false)); // REI autoselected

        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, false, true)); // REI two-way
        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, true, true)); // REI two-way autoselected
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
