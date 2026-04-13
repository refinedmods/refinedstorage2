package com.refinedmods.refinedstorage.common.storage.externalstorage;

import com.refinedmods.refinedstorage.common.storage.AbstractStorageScreen;
import com.refinedmods.refinedstorage.common.support.AbstractFilterScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ExternalStorageScreen extends AbstractStorageScreen<ExternalStorageContainerMenu> {
    public ExternalStorageScreen(final ExternalStorageContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title, 176, 137);
        this.inventoryLabelY = 42;
    }

    @Override
    protected Identifier getTexture() {
        return AbstractFilterScreen.TEXTURE;
    }
}
