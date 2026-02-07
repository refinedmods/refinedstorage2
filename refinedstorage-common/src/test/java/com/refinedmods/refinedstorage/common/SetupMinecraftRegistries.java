package com.refinedmods.refinedstorage.common;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

class SetupMinecraftRegistries implements BeforeAllCallback {
    @Override
    @SuppressWarnings("deprecation")
    public void beforeAll(final ExtensionContext context) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        for (final Item item : BuiltInRegistries.ITEM) {
            item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                .set(DataComponents.ITEM_NAME, Component.translatable(item.getDescriptionId()))
                .build());
        }
    }
}

