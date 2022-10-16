package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;

public class InterfaceScreen extends AbstractBaseScreen<InterfaceContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/interface.png");

    public InterfaceScreen(final InterfaceContainerMenu menu,
                           final Inventory playerInventory,
                           final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 88;
        this.imageWidth = 176;
        this.imageHeight = 182;
    }

    @Override
    protected boolean isResourceFilterButtonVisible() {
        return false;
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
