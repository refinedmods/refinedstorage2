package com.refinedmods.refinedstorage2.platform.common.storage.externalstorage;

import com.refinedmods.refinedstorage2.platform.common.storage.AbstractStorageScreen;
import com.refinedmods.refinedstorage2.platform.common.support.AbstractFilterScreen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExternalStorageScreen extends AbstractStorageScreen<ExternalStorageContainerMenu> {
    public ExternalStorageScreen(final ExternalStorageContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title);
        this.inventoryLabelY = 42;
        this.imageWidth = 210;
        this.imageHeight = 137;
    }

    @Override
    protected ResourceLocation getTexture() {
        return AbstractFilterScreen.TEXTURE;
    }
}
