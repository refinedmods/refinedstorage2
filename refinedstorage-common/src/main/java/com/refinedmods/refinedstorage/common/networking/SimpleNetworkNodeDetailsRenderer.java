package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.impl.node.SimpleNetworkNodeDetails;
import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.common.api.networking.NetworkNodeDetailsRenderer;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class SimpleNetworkNodeDetailsRenderer implements NetworkNodeDetailsRenderer {
    @Override
    public List<ClientTooltipComponent> render(final NetworkNodeDetails details, final GuiGraphicsExtractor graphics,
                                               final int x, final int y, final int mouseX, final int mouseY) {
        if (!(details instanceof SimpleNetworkNodeDetails simpleDetails)) {
            return Collections.emptyList();
        }
        final Font font = Minecraft.getInstance().font;
        graphics.text(
            font,
            "Energy usage: " + simpleDetails.getEnergyUsage() + " FE/t",
            x + 1,
            y + 1,
            -12566464,
            false
        );
        graphics.text(
            font,
            "Active: " + simpleDetails.isActive() + " FE/t",
            x + 1,
            y + 1 + 18,
            -12566464,
            false
        );
        return Collections.emptyList();
    }

    @Override
    public int getRows(final NetworkNodeDetails details) {
        return 2;
    }
}
