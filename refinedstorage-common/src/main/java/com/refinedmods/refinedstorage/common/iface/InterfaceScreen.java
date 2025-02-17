package com.refinedmods.refinedstorage.common.iface;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.FuzzyModeSideButtonWidget;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.AbstractFilterScreen.renderExportingIndicators;
import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public class InterfaceScreen extends AbstractBaseScreen<InterfaceContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/interface.png");

    public InterfaceScreen(final InterfaceContainerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
        this.inventoryLabelY = 88;
        this.imageWidth = 210;
        this.imageHeight = 182;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
        addSideButton(new FuzzyModeSideButtonWidget(
            getMenu().getProperty(PropertyTypes.FUZZY_MODE),
            () -> FuzzyModeSideButtonWidget.Type.EXTRACTING_STORAGE_NETWORK
        ));
    }

    @Override
    protected void renderTooltip(final GuiGraphics graphics, final int x, final int y) {
        if (renderExportingIndicators(graphics, leftPos, topPos + WARNING_SIZE - 18 - 2, x, y,
            getMenu().getIndicators(), getMenu()::getIndicator)) {
            return;
        }
        super.renderTooltip(graphics, x, y);
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }
}
