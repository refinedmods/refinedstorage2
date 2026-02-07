package com.refinedmods.refinedstorage.common.support;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.common.api.RefinedStorageClientApi;
import com.refinedmods.refinedstorage.common.api.support.resource.ResourceRendering;
import com.refinedmods.refinedstorage.common.support.containermenu.ResourceSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ResourceSlotRendering {
    private ResourceSlotRendering() {
    }

    public static void render(final GuiGraphicsExtractor graphics, final ResourceSlot slot) {
        final ResourceKey resource = slot.getResource();
        if (resource == null) {
            return;
        }
        render(graphics, slot.x, slot.y, resource, slot.getAmount(), slot.shouldRenderAmount());
    }

    private static void render(final GuiGraphicsExtractor graphics,
                               final int x,
                               final int y,
                               final ResourceKey resource,
                               final long amount,
                               final boolean renderAmount) {
        final ResourceRendering rendering = RefinedStorageClientApi.INSTANCE.getResourceRendering(resource.getClass());
        rendering.render(resource, graphics, x, y);
        if (renderAmount) {
            renderAmount(graphics, x, y, amount, rendering);
        }
    }

    public static void renderAmount(final GuiGraphicsExtractor graphics,
                                    final int x,
                                    final int y,
                                    final long amount,
                                    final ResourceRendering rendering) {
        final String formattedAmount = rendering.formatAmount(amount, true);
        final boolean large = Minecraft.getInstance().font.width(formattedAmount) <= 16;
        renderAmount(
            graphics,
            x,
            y,
            formattedAmount,
            0xFFFFFFFF,
            large
        );
    }

    public static void renderAmount(final GuiGraphicsExtractor graphics,
                                    final int x,
                                    final int y,
                                    final String text,
                                    final int color,
                                    final boolean large) {
        final Font font = Minecraft.getInstance().font;
        graphics.pose().pushMatrix();
        // Large amounts overlap with the slot lines (see Minecraft behavior)
        graphics.pose().translate(x + (large ? 1F : 0F), y + (large ? 1F : 0F));
        if (!large) {
            graphics.pose().scale(0.5F, 0.5F);
        }
        graphics.text(font, text, (large ? 16 : 30) - font.width(text), large ? 8 : 22, color, true);
        graphics.pose().popMatrix();
    }
}
