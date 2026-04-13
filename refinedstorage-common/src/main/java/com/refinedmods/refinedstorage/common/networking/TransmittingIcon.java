package com.refinedmods.refinedstorage.common.networking;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED;

class TransmittingIcon {
    private static final int WIDTH_0 = 11;
    private static final int WIDTH_3 = 20;
    private static final int TRANSMITTING_FRAMES = 20;
    private static final Identifier NOT_TRANSMITTING = createIdentifier("transmitting/0");
    private static final Identifier TRANSMITTING_1 = createIdentifier("transmitting/1");
    private static final Identifier TRANSMITTING_2 = createIdentifier("transmitting/2");
    private static final Identifier TRANSMITTING_3 = createIdentifier("transmitting/3");

    private int frames;
    private int cycle;
    private boolean active;

    TransmittingIcon(final boolean active) {
        this.active = active;
    }

    void tick(final boolean newActive) {
        this.active = newActive;
        doTick();
    }

    private void doTick() {
        if (!active) {
            frames = 0;
            cycle = 0;
            return;
        }
        ++frames;
        if (frames == TRANSMITTING_FRAMES) {
            frames = 0;
            cycle++;
        }
    }

    void render(final GuiGraphicsExtractor graphics, final int x3, final int y3) {
        if (!active) {
            graphics.blitSprite(GUI_TEXTURED, NOT_TRANSMITTING, x3, y3 + 4, WIDTH_0, 4);
            return;
        }
        final int frame = cycle % 3;
        switch (frame) {
            case 1:
                graphics.blitSprite(GUI_TEXTURED, TRANSMITTING_2, x3, y3 + 1, 17, 10);
                break;
            case 2:
                graphics.blitSprite(GUI_TEXTURED, TRANSMITTING_3, x3, y3, WIDTH_3, 12);
                break;
            case 0:
            default:
                graphics.blitSprite(GUI_TEXTURED, TRANSMITTING_1, x3, y3 + 3, 14, 6);
                break;
        }
    }

    int getWidth() {
        return active ? WIDTH_3 : WIDTH_0;
    }
}
