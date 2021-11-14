package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.internal.grid.search.PlatformSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.grid.GridScreenHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SearchBoxModeSideButtonWidget extends SideButtonWidget {
    private final GridScreenHandler screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<PlatformSearchBoxModeImpl, List<Text>> tooltips = new HashMap<>();

    public SearchBoxModeSideButtonWidget(GridScreenHandler screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static PressAction createPressAction(GridScreenHandler screenHandler) {
        return btn -> screenHandler.setSearchBoxMode((PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.next(screenHandler.getSearchBoxMode()));
    }

    private List<Text> calculateTooltip(PlatformSearchBoxModeImpl searchBoxMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "grid.search_box_mode"));
        lines.add(searchBoxMode.getName().formatted(Formatting.GRAY));
        return lines;
    }

    @Override
    protected Identifier getTextureIdentifier() {
        return screenHandler.getSearchBoxMode().getTextureIdentifier();
    }

    @Override
    protected int getXTexture() {
        return screenHandler.getSearchBoxMode().getTextureX();
    }

    @Override
    protected int getYTexture() {
        return screenHandler.getSearchBoxMode().getTextureY();
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.computeIfAbsent(screenHandler.getSearchBoxMode(), this::calculateTooltip), mouseX, mouseY);
    }
}
