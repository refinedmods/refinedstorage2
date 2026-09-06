package com.refinedmods.refinedstorage.common.api.networking;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeDetails;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepository;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import org.apiguardian.api.API;
import org.jspecify.annotations.Nullable;

@API(status = API.Status.STABLE, since = "3.3.0")
public interface NetworkNodeDetailsRenderer {
    List<ClientTooltipComponent> render(NetworkNodeDetails details, GuiGraphicsExtractor graphics, int x, int scrollY,
                                        int baseY, int width, int height, int mouseX, int mouseY);

    int getHeight(NetworkNodeDetails details);

    default void setRepository(@Nullable final ResourceRepository<GridResource> repository) {
        // no op
    }

    default List<AbstractWidget> createWidgets(final int width) {
        return Collections.emptyList();
    }
}
