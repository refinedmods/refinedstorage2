package com.refinedmods.refinedstorage2.platform.api.grid;

import com.refinedmods.refinedstorage2.api.grid.service.GridExtractMode;
import com.refinedmods.refinedstorage2.api.grid.view.GridResource;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.6")
public interface PlatformGridResource extends GridResource {
    void onExtract(GridExtractMode extractMode,
                   boolean cursor,
                   GridExtractionStrategy extractionStrategy);

    void onScroll(GridScrollMode scrollMode,
                  GridScrollingStrategy scrollingStrategy);

    void render(GuiGraphics graphics, int x, int y);

    String getDisplayedAmount();

    String getAmountInTooltip();

    List<Component> getTooltip();

    int getRegistryId();
}
