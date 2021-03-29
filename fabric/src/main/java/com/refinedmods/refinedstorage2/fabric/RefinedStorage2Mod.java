package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.core.RefinedStorage2ApiFacade;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeDisplayProperties;
import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParser;
import com.refinedmods.refinedstorage2.core.grid.query.GridQueryParserImpl;
import com.refinedmods.refinedstorage2.core.query.lexer.LexerTokenMappings;
import com.refinedmods.refinedstorage2.core.query.parser.ParserOperatorMappings;
import com.refinedmods.refinedstorage2.fabric.coreimpl.FabricRefinedStorage2ApiFacade;
import com.refinedmods.refinedstorage2.fabric.coreimpl.grid.ReiGridSearchBoxMode;
import com.refinedmods.refinedstorage2.fabric.init.RefinedStorage2BlockEntities;
import com.refinedmods.refinedstorage2.fabric.init.RefinedStorage2Blocks;
import com.refinedmods.refinedstorage2.fabric.init.RefinedStorage2Items;
import com.refinedmods.refinedstorage2.fabric.init.RefinedStorage2ScreenHandlers;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridChangeSettingPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridExtractPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridInsertFromCursorPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.GridScrollPacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.PropertyChangePacket;
import com.refinedmods.refinedstorage2.fabric.packet.c2s.StorageDiskInfoRequestPacket;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RefinedStorage2Mod implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(RefinedStorage2Mod.class);

    public static final String ID = "refinedstorage2";

    public static final RefinedStorage2ApiFacade API = new FabricRefinedStorage2ApiFacade();

    public static final RefinedStorage2Blocks BLOCKS = new RefinedStorage2Blocks();
    public static final RefinedStorage2Items ITEMS = new RefinedStorage2Items();
    public static final RefinedStorage2BlockEntities BLOCK_ENTITIES = new RefinedStorage2BlockEntities();
    public static final RefinedStorage2ScreenHandlers SCREEN_HANDLERS = new RefinedStorage2ScreenHandlers();

    private static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(ID, "general"), () -> new ItemStack(BLOCKS.getCable()));

    @Override
    public void onInitialize() {
        AutoConfig.register(RefinedStorage2Config.class, Toml4jConfigSerializer::new);

        BLOCKS.register();
        ITEMS.register(BLOCKS, ITEM_GROUP);
        BLOCK_ENTITIES.register(BLOCKS);
        SCREEN_HANDLERS.register();

        GridQueryParser queryParser = new GridQueryParserImpl(LexerTokenMappings.DEFAULT_MAPPINGS, ParserOperatorMappings.DEFAULT_MAPPINGS);

        for (boolean autoSelected : new boolean[]{false, true}) {
            API.getGridSearchBoxModeRegistry().add(new GridSearchBoxModeImpl(queryParser, autoSelected, new GridSearchBoxModeDisplayProperties(
                new Identifier(ID, "textures/icons.png"),
                autoSelected ? 16 : 0,
                96,
                new TranslatableText("gui.refinedstorage2.grid.search_box_mode.normal" + (autoSelected ? "_autoselected" : "")).formatted(Formatting.GRAY)
            )));
        }

        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, false, false)); // REI
        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, true, false)); // REI autoselected

        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, false, true)); // REI two-way
        API.getGridSearchBoxModeRegistry().add(ReiGridSearchBoxMode.create(queryParser, true, true)); // REI two-way autoselected

        ServerSidePacketRegistry.INSTANCE.register(StorageDiskInfoRequestPacket.ID, new StorageDiskInfoRequestPacket());
        ServerSidePacketRegistry.INSTANCE.register(GridInsertFromCursorPacket.ID, new GridInsertFromCursorPacket());
        ServerSidePacketRegistry.INSTANCE.register(GridExtractPacket.ID, new GridExtractPacket());
        ServerSidePacketRegistry.INSTANCE.register(GridScrollPacket.ID, new GridScrollPacket());
        ServerSidePacketRegistry.INSTANCE.register(GridChangeSettingPacket.ID, new GridChangeSettingPacket());
        ServerSidePacketRegistry.INSTANCE.register(PropertyChangePacket.ID, new PropertyChangePacket());

        LOGGER.info("Refined Storage 2 has loaded.");
    }
}
