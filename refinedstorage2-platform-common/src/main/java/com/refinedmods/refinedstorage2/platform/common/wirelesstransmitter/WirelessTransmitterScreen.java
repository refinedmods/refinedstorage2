package com.refinedmods.refinedstorage2.platform.common.wirelesstransmitter;

import com.refinedmods.refinedstorage2.platform.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage2.platform.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage2.platform.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class WirelessTransmitterScreen extends AbstractBaseScreen<WirelessTransmitterContainerMenu> {
    private static final ResourceLocation TEXTURE = createIdentifier("textures/gui/wireless_transmitter.png");

    public WirelessTransmitterScreen(final WirelessTransmitterContainerMenu containerMenu,
                                     final Inventory inventory,
                                     final Component title) {
        super(containerMenu, inventory, title);
        this.inventoryLabelY = 43;
        this.imageWidth = 211;
        this.imageHeight = 137;
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected ResourceLocation getTexture() {
        return TEXTURE;
    }

    @Override
    protected void renderLabels(final GuiGraphics graphics, final int mouseX, final int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(
            font,
            createTranslation("gui", "wireless_transmitter.distance", getMenu().getRange()),
            28,
            25,
            4210752,
            false
        );
    }
}
