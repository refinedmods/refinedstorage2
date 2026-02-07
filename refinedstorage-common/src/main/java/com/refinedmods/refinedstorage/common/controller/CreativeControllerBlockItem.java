package com.refinedmods.refinedstorage.common.controller;

import com.refinedmods.refinedstorage.common.support.NetworkNodeBlockItem;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class CreativeControllerBlockItem extends NetworkNodeBlockItem {
    private static final MutableComponent HELP = createTranslation("item", "creative_controller.help");

    CreativeControllerBlockItem(final Block block, final Identifier id) {
        super(block, new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, id))
            .useBlockDescriptionPrefix().overrideDescription(block.getDescriptionId())
            .stacksTo(1), HELP);
    }
}
