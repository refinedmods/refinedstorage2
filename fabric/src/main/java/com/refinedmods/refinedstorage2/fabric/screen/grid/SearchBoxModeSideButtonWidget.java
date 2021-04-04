package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class SearchBoxModeSideButtonWidget extends SideButtonWidget {
    private final GridScreenHandler screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSearchBoxMode, List<Text>> tooltips = new HashMap<>();

    public SearchBoxModeSideButtonWidget(GridScreenHandler screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
    }

    private List<Text> calculateTooltip(GridSearchBoxMode searchBoxMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(new TranslatableText("gui.refinedstorage2.grid.search_box_mode"));
        lines.add(searchBoxMode.getDisplayProperties().getName());
        return lines;
    }

    private static PressAction createPressAction(GridScreenHandler screenHandler) {
        return btn -> screenHandler.setSearchBoxMode(RefinedStorage2Mod.API.getGridSearchBoxModeRegistry().next(screenHandler.getSearchBoxMode()));
    }

    @Override
    protected Identifier getSpriteIdentifier() {
        return screenHandler.getSearchBoxMode().getDisplayProperties().getSpriteIdentifier();
    }

    @Override
    protected int getXTexture() {
        return screenHandler.getSearchBoxMode().getDisplayProperties().getX();
    }

    @Override
    protected int getYTexture() {
        return screenHandler.getSearchBoxMode().getDisplayProperties().getY();
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.computeIfAbsent(screenHandler.getSearchBoxMode(), this::calculateTooltip), mouseX, mouseY);
    }
}
