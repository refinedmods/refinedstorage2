package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class SimpleNetworkNodeDetailsRenderer extends AbstractNetworkNodeDetailsRenderer {
    @Override
    protected List<ClientTooltipComponent> renderAdditionalDetails(final NetworkNodeDetails details,
                                                                   final GuiGraphicsExtractor graphics, final int x,
                                                                   final int scrollY,
                                                                   final int baseY, final int width, final int height,
                                                                   final int mouseX,
                                                                   final int mouseY) {
        return Collections.emptyList();
    }

    @Override
    protected int getAdditionalHeight(final NetworkNodeDetails details) {
        return 0;
    }
}
