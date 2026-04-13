package com.refinedmods.refinedstorage.common.storage.storageblock;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.storage.AbstractProgressStorageScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class StorageBlockScreen extends AbstractProgressStorageScreen<StorageBlockContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/storage.png");

    private final ResourceRendering resourceRendering;

    public StorageBlockScreen(final StorageBlockContainerMenu menu,
                              final Inventory inventory,
                              final Component title,
                              final ResourceRendering resourceRendering) {
        super(menu, inventory, title, 80);
        this.resourceRendering = resourceRendering;
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    protected String formatAmount(final long qty) {
        return resourceRendering.formatAmount(qty);
    }
}
