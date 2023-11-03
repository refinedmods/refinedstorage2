package com.refinedmods.refinedstorage2.platform.common.grid.screen;

import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class GridScreen<T extends AbstractGridContainerMenu> extends AbstractGridScreen<T> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/grid.png");

    public GridScreen(final T menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 99);
        this.inventoryLabelY = 75;
        this.imageWidth = 227;
        this.imageHeight = 176;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
