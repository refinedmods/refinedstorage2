package com.refinedmods.refinedstorage2.fabric;

import com.refinedmods.refinedstorage2.fabric.block.CableBlock;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RefinedStorage2Mod implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger(RefinedStorage2Mod.class);

    private static final String ID = "refinedstorage2";

    private static final ItemGroup ITEM_GROUP = FabricItemGroupBuilder.build(new Identifier(ID, "general"), () -> new ItemStack(Items.COBBLESTONE));

    public static final CableBlock CABLE_BLOCK = new CableBlock();
    private static final BlockItem CABLE_ITEM = new BlockItem(CABLE_BLOCK, new Item.Settings().group(ITEM_GROUP));

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(ID, "cable"), CABLE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(ID, "cable"), CABLE_ITEM);

        LOGGER.info("Refined Storage 2 has loaded.");
    }
}
