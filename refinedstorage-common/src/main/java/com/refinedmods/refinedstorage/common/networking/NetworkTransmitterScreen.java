package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.AbstractBaseScreen;
import com.refinedmods.refinedstorage.common.support.containermenu.PropertyTypes;
import com.refinedmods.refinedstorage.common.support.widget.RedstoneModeSideButtonWidget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING;
import static com.refinedmods.refinedstorage.common.support.Sprites.WARNING_SIZE;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

public class NetworkTransmitterScreen extends AbstractBaseScreen<NetworkTransmitterContainerMenu> {
    private static final Identifier TEXTURE = createIdentifier("textures/gui/network_transmitter.png");

    private final TransmittingIcon icon;

    public NetworkTransmitterScreen(final NetworkTransmitterContainerMenu menu,
                                    final Inventory playerInventory,
                                    final Component title) {
        super(menu, playerInventory, title, 176, 137);
        this.inventoryLabelY = 42;
        this.icon = new TransmittingIcon(isIconActive());
    }

    @Override
    protected void init() {
        super.init();
        addSideButton(new RedstoneModeSideButtonWidget(getMenu().getProperty(PropertyTypes.REDSTONE_MODE)));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        icon.tick(isIconActive());
    }

    private boolean isIconActive() {
        return !getMenu().getStatus().error() && getMenu().getStatus().transmitting();
    }

    @Override
    public void extractBackground(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                  final float partialTicks) {
        super.extractBackground(graphics, mouseX, mouseY, partialTicks);
        icon.render(graphics, leftPos + 29, topPos + 22);
    }

    @Override
    protected void extractLabels(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        final NetworkTransmitterData status = getMenu().getStatus();
        final int x = 25 + 4 + icon.getWidth() + 4;
        if (status.error()) {
            graphics.blitSprite(GUI_TEXTURED, WARNING, x, 23, WARNING_SIZE, WARNING_SIZE);
        }
        graphics.text(font, status.message(), x + (status.error() ? (10 + 4) : 0), 25, -12566464, false);
    }

    @Override
    protected Identifier getTexture() {
        return TEXTURE;
    }
}
