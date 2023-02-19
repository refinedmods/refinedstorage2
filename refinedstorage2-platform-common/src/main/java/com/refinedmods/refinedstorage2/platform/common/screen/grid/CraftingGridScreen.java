package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.CraftingGridContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class CraftingGridScreen extends AbstractGridScreen<CraftingGridContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/crafting_grid.png");

    public CraftingGridScreen(final CraftingGridContainerMenu menu, final Inventory inventory, final Component title) {
        super(menu, inventory, title, 156);
        this.inventoryLabelY = 134;
        this.imageWidth = 227;
        this.imageHeight = 229;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
