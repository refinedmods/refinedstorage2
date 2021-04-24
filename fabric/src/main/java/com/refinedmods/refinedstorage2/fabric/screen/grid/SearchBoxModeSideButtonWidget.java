package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class SearchBoxModeSideButtonWidget extends SideButtonWidget {
    private final GridScreenHandler screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSearchBoxMode, List<Text>> tooltips = new HashMap<>();
    private final Map<String, Identifier> identifiers = new HashMap<>();

    public SearchBoxModeSideButtonWidget(GridScreenHandler screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static PressAction createPressAction(GridScreenHandler screenHandler) {
        return btn -> screenHandler.setSearchBoxMode(Rs2Mod.API.getGridSearchBoxModeRegistry().next(screenHandler.getSearchBoxMode()));
    }

    private List<Text> calculateTooltip(GridSearchBoxMode searchBoxMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "grid.search_box_mode"));
        lines.add(new TranslatableText(searchBoxMode.getDisplayProperties().getNameTranslationKey()).formatted(Formatting.GRAY));
        return lines;
    }

    @Override
    protected Identifier getTextureIdentifier() {
        return identifiers.computeIfAbsent(screenHandler.getSearchBoxMode().getDisplayProperties().getTextureIdentifier(), Identifier::new);
    }

    @Override
    protected int getXTexture() {
        return screenHandler.getSearchBoxMode().getDisplayProperties().getTextureX();
    }

    @Override
    protected int getYTexture() {
        return screenHandler.getSearchBoxMode().getDisplayProperties().getTextureY();
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.computeIfAbsent(screenHandler.getSearchBoxMode(), this::calculateTooltip), mouseX, mouseY);
    }
}
