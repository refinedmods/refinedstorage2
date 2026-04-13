package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class GridScreen<T extends AbstractGridContainerMenu> extends AbstractGridScreen<T> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/grid.png");

    public GridScreen(final T menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 99, 193, 176);
        this.inventoryLabelY = 75;
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
