package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;
import com.refinedmods.refinedstorage.common.support.widget.TextMarquee;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class NetworkMonitorNetworkWidget extends Button {
    private static final int WIDTH = 64;
    private static final int HEIGHT = 18;
    private static final Component NETWORK = createTranslation("gui", "network_monitor.network");

    private final TextMarquee text;
    private boolean outOfFrame;

    NetworkMonitorNetworkWidget(final int x, final int y, final Runnable onPress, final boolean active) {
        super(x, y, WIDTH, HEIGHT, NETWORK, btn -> onPress.run(), DEFAULT_NARRATION);
        this.active = active;
        this.text = new TextMarquee(
            NETWORK,
            WIDTH - 4 - 4,
            0xFFFFFFFF,
            true,
            TextMarquee.Style.SMALL
        );
    }

    void setOutOfFrame(final boolean outOfFrame) {
        this.outOfFrame = outOfFrame;
    }

    @Override
    public int getHeight() {
        if (!visible) {
            return 0;
        }
        return super.getHeight();
    }

    @Override
    protected void extractContents(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY,
                                   final float partialTicks) {
        if (outOfFrame) {
            return;
        }
        extractDefaultSprite(graphics);
        final int yOffset = SmallText.isSmall() ? 6 : 3;
        final int textX = getX() + 4;
        final int textY = getY() + yOffset;
        text.updateStateAndRender(graphics, textX, textY, Minecraft.getInstance().font, isHovered, partialTicks);
    }
}
