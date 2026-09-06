package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.AbstractNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.networking.NetworkNodeDetailsRenderer;
import com.refinedmods.refinedstorage.common.support.tooltip.SmallText;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.format;

public abstract class AbstractNetworkNodeDetailsRenderer implements NetworkNodeDetailsRenderer {
    protected static final int PADDING = 4;
    private static final int TOP_PADDING = 5;

    private static final Component ACTIVE = createTranslation("gui", "network_monitor.details.active");
    private static final Component INACTIVE = createTranslation("gui", "network_monitor.details.inactive");

    @Override
    public final List<ClientTooltipComponent> render(final NetworkNodeDetails details,
                                                     final GuiGraphicsExtractor graphics,
                                                     final int x,
                                                     final int scrollY,
                                                     final int baseY,
                                                     final int width,
                                                     final int height,
                                                     final int mouseX,
                                                     final int mouseY) {
        if (!(details instanceof AbstractNetworkNodeDetails baseDetails)) {
            return Collections.emptyList();
        }
        renderBase(graphics, x, scrollY, width, baseDetails);
        return renderAdditionalDetails(details, graphics, x, scrollY + getHeaderHeight(), baseY, width, height,
            mouseX, mouseY);
    }

    private static void renderBase(final GuiGraphicsExtractor graphics, final int x, final int y, final int width,
                                   final AbstractNetworkNodeDetails baseDetails) {
        final Font font = Minecraft.getInstance().font;
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        final Component energyUsage = createTranslation("gui", "network_monitor.details.energy_usage_per_tick",
            format(baseDetails.getEnergyUsage()));
        SmallText.render(graphics, font, energyUsage.getVisualOrderText(), x + PADDING, y + TOP_PADDING, 0xFF404040,
            false, scale);
        final Component activeInactive = baseDetails.isActive() ? ACTIVE : INACTIVE;
        SmallText.render(graphics, font, activeInactive.getVisualOrderText(),
            x + width - PADDING - (int) (font.width(activeInactive) * scale), y + TOP_PADDING, 0xFF404040, false,
            scale);
    }

    @Override
    public final int getHeight(final NetworkNodeDetails details) {
        return getHeaderHeight() + getAdditionalHeight(details);
    }

    protected static int getLineHeight() {
        final float scale = SmallText.correctScale(SmallText.DEFAULT_SCALE);
        return (int) (Minecraft.getInstance().font.lineHeight * scale) + PADDING;
    }

    private static int getHeaderHeight() {
        return TOP_PADDING + getLineHeight();
    }

    protected abstract List<ClientTooltipComponent> renderAdditionalDetails(NetworkNodeDetails details,
                                                                            GuiGraphicsExtractor graphics,
                                                                            int x,
                                                                            int scrollY,
                                                                            int baseY,
                                                                            int width,
                                                                            int height,
                                                                            int mouseX,
                                                                            int mouseY);

    protected abstract int getAdditionalHeight(NetworkNodeDetails details);
}
