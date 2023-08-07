package com.refinedmods.refinedstorage2.platform.api.resource;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.13")
public interface ResourceRendering<T> {
    String getDisplayedAmount(long amount);

    Component getDisplayName(T resource);

    List<Component> getTooltip(T resource);

    void render(T resource, GuiGraphics graphics, int x, int y);
}
