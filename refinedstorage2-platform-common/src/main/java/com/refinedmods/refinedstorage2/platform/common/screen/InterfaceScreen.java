package com.refinedmods.refinedstorage2.platform.common.screen;

import com.refinedmods.refinedstorage2.platform.common.containermenu.InterfaceContainerMenu;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class InterfaceScreen extends AbstractBaseScreen<InterfaceContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/interface.png");
    private static final MutableComponent EXPORT_TEXT = createTranslation("gui", "interface.export");

    public InterfaceScreen(final InterfaceContainerMenu menu,
                           final Inventory playerInventory,
                           final Component text) {
        super(menu, playerInventory, text);
        this.inventoryLabelY = 122;
        this.imageWidth = 211;
        this.imageHeight = 217;
    }

    @Override
    protected boolean isResourceFilterButtonVisible() {
        return false;
    }

    @Override
    protected void renderLabels(final PoseStack poseStack, final int mouseX, final int mouseY) {
        super.renderLabels(poseStack, mouseX, mouseY);
        font.draw(poseStack, EXPORT_TEXT, 7, 42, 4210752);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
