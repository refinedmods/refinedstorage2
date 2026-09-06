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
    protected static final int WIDTH = 162;
    protected static final int TEXT_COLOR = 0xFF404040;

    private static final Component ACTIVE = createTranslation("gui", "network_monitor.details.active");
    private static final Component INACTIVE = createTranslation("gui", "network_monitor.details.inactive");
    private static final int TOP_PADDING = 5;

    @Override
    public final List<ClientTooltipComponent> render(final NetworkNodeDetails details,
                                                     final GuiGraphicsExtractor graphics,
                                                     final int x,
                                                     final int y,
                                                     final int visibleY,
                                                     final int visibleHeight,
                                                     final int mouseX,
                                                     final int mouseY) {
        if (!(details instanceof AbstractNetworkNodeDetails baseDetails)) {
            return Collections.emptyList();
        }
        final Font font = Minecraft.getInstance().font;
        final float scale = smallTextScale();
        final Component energyUsage = createTranslation("gui", "network_monitor.details.energy_usage_per_tick",
            format(baseDetails.getEnergyUsage()));
        SmallText.render(graphics, font, energyUsage.getVisualOrderText(), x + PADDING, y + TOP_PADDING, TEXT_COLOR,
            false, scale);
        final Component activeInactive = baseDetails.isActive() ? ACTIVE : INACTIVE;
        SmallText.render(graphics, font, activeInactive.getVisualOrderText(),
            x + WIDTH - PADDING - (int) (font.width(activeInactive) * scale), y + TOP_PADDING, TEXT_COLOR, false,
            scale);
        return renderDetails(details, graphics, x, y + getHeaderHeight(), visibleY, visibleHeight, mouseX, mouseY);
    }

    @Override
    public final int getHeight(final NetworkNodeDetails details) {
        return getHeaderHeight() + getDetailsHeight(details);
    }

    protected static float smallTextScale() {
        return SmallText.correctScale(SmallText.DEFAULT_SCALE);
    }

    protected static int smallTextLineHeight() {
        return (int) (Minecraft.getInstance().font.lineHeight * smallTextScale()) + PADDING;
    }

    private static int getHeaderHeight() {
        return TOP_PADDING + smallTextLineHeight();
    }

    protected abstract List<ClientTooltipComponent> renderDetails(NetworkNodeDetails details,
                                                                  GuiGraphicsExtractor graphics,
                                                                  int x,
                                                                  int y,
                                                                  int visibleY,
                                                                  int visibleHeight,
                                                                  int mouseX,
                                                                  int mouseY);

    protected abstract int getDetailsHeight(NetworkNodeDetails details);
}
