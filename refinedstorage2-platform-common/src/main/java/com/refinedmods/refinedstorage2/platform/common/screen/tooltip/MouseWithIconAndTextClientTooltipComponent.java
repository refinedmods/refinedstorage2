package com.refinedmods.refinedstorage2.platform.common.screen.tooltip;

import com.refinedmods.refinedstorage2.platform.api.PlatformApi;
import com.refinedmods.refinedstorage2.platform.common.internal.resource.filter.item.ItemFilteredResource;
import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

public class MouseWithIconAndTextClientTooltipComponent implements ClientTooltipComponent {
    private static final int MOUSE_ICON_WIDTH = 9;
    private static final int MOUSE_ICON_HEIGHT = 16;

    private final boolean left;
    private final IconRenderer iconRenderer;
    @Nullable
    private final String amount;

    public MouseWithIconAndTextClientTooltipComponent(final boolean left,
                                                      final IconRenderer iconRenderer,
                                                      @Nullable final String amount) {
        this.left = left;
        this.iconRenderer = iconRenderer;
        this.amount = amount;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public int getWidth(final Font font) {
        return MOUSE_ICON_WIDTH + 4 + (amount == null ? 16 : 19 - 2);
    }

    @Override
    public void renderImage(final Font font, final int x, final int y, final GuiGraphics graphics) {
        final int u = left ? 238 : 247;
        final int v = 178;
        graphics.blit(TextureIds.ICONS, x, y, u, v, MOUSE_ICON_WIDTH, MOUSE_ICON_HEIGHT);
        iconRenderer.render(graphics, x + MOUSE_ICON_WIDTH + 4, y);
        if (amount != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 200.0F);
            graphics.drawString(font, amount, x + MOUSE_ICON_WIDTH + 4 + 19 - 2 - font.width(amount), y + 6 + 3,
                16777215, true);
            graphics.pose().popPose();
        }
    }

    public static List<ClientTooltipComponent> createForFilter(final ItemStack carried) {
        if (carried.isEmpty()) {
            return Collections.emptyList();
        }
        final List<ClientTooltipComponent> lines = new ArrayList<>();
        PlatformApi.INSTANCE.getFilteredResourceFactory()
            .create(carried, false)
            .ifPresent(asItem -> lines.add(
                new MouseWithIconAndTextClientTooltipComponent(true, asItem::render, null)
            ));
        PlatformApi.INSTANCE.getFilteredResourceFactory()
            .create(carried, true)
            .ifPresent(asAlternative -> {
                if (asAlternative instanceof ItemFilteredResource) {
                    return;
                }
                lines.add(new MouseWithIconAndTextClientTooltipComponent(false, asAlternative::render, null));
            });
        return lines;
    }

    @FunctionalInterface
    public interface IconRenderer {
        void render(GuiGraphics graphics, int x, int y);
    }
}
