package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.ExternalStorageContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class ExternalStorageScreen extends AbstractStorageScreen<ExternalStorageContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/storage.png");

    public ExternalStorageScreen(final ExternalStorageContainerMenu menu,
                                 final Inventory inventory,
                                 final Component title) {
        super(menu, inventory, title, 80);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
