package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class WirelessTransmitterScreen extends AbstractBaseScreen<WirelessTransmitterContainerMenu> {
    private static final MutableComponent INACTIVE = createTranslation("gui", "wireless_transmitter.inactive");
    private static final Identifier TEXTURE = createIdentifier("textures/gui/wireless_transmitter.png");

    private final TransmittingIcon icon;

    public WirelessTransmitterScreen(final WirelessTransmitterContainerMenu containerMenu,
                                     final Inventory inventory,
                                     final Component title) {
        super(containerMenu, inventory, title, 211, 137);
        this.inventoryLabelY = 43;
        this.icon = new TransmittingIcon(getMenu().isActive());
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        icon.tick(getMenu().isActive());
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        icon.render(graphics, leftPos + 7, topPos + 22);
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (!getMenu().isActive()) {
            graphics.text(font, INACTIVE, 7 + icon.getWidth() + 4, 25, -12566464, false);
            return;
        }
        graphics.text(
            font,
            createTranslation("gui", "wireless_transmitter.distance", getMenu().getRange()),
            7 + icon.getWidth() + 4,
            25,
            -12566464,
            false
        );
    }
}
