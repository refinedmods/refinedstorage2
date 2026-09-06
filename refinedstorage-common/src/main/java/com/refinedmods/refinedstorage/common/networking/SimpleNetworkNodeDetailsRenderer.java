package com.refinedmods.refinedstorage.common.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

public class SimpleNetworkNodeDetailsRenderer extends AbstractNetworkNodeDetailsRenderer {
    @Override
    protected List<ClientTooltipComponent> renderDetails(final NetworkNodeDetails details,
                                                         final GuiGraphicsExtractor graphics,
                                                         final int x,
                                                         final int y,
                                                         final int visibleY,
                                                         final int visibleHeight,
                                                         final int mouseX,
                                                         final int mouseY) {
        return Collections.emptyList();
    }

    @Override
    protected int getDetailsHeight(final NetworkNodeDetails details) {
        return 0;
    }
}
