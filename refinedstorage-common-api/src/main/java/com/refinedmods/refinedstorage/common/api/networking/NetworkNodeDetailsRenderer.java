package com.refinedmods.refinedstorage.common.api.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "3.3.0")
public interface NetworkNodeDetailsRenderer {
    List<ClientTooltipComponent> render(NetworkNodeDetails details, GuiGraphicsExtractor graphics, int x, int y,
                                        int mouseX, int mouseY);

    int getRows(NetworkNodeDetails details);
}
