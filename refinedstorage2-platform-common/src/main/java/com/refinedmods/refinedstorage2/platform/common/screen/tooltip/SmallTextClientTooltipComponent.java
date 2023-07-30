package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

public class SmallTextClientTooltipComponent implements ClientTooltipComponent {
    private final List<? extends Component> components;
    private final float scale;

    public SmallTextClientTooltipComponent(final List<? extends Component> components) {
        this.components = components;
        this.scale = (Minecraft.getInstance().isEnforceUnicode()) ? 1F : 0.7F;
    }

    @Override
    public void renderText(final Font font,
                           final int x,
                           final int y,
                           final Matrix4f pose,
                           final MultiBufferSource.BufferSource buffer) {
        final Matrix4f scaled = new Matrix4f(pose);
        scaled.scale(scale, scale, 1);
        float yy = (y / scale) + 1;
        for (final Component component : components) {
            font.drawInBatch(
                component,
                x / scale,
                yy,
                -1,
                true,
                scaled,
                buffer,
                Font.DisplayMode.NORMAL,
                0,
                15728880
            );
            yy += 9;
            yy += 2;
        }
    }

    @Override
    public int getHeight() {
        return (components.size() * 9) + ((components.size() - 1) * 2);
    }

    @Override
    public int getWidth(final Font font) {
        int width = 0;
        for (final Component component : components) {
            final int componentWidth = (int) (font.width(component) * scale);
            if (componentWidth > width) {
                width = componentWidth;
            }
        }
        return width;
    }
}
