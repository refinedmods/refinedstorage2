package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.grid.GridSearchBoxMode;
import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.handler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class SearchBoxModeSideButtonWidget extends SideButtonWidget {
    private final TwoWaySyncProperty<GridSearchBoxMode> searchBoxModeProperty;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSearchBoxMode, List<Text>> tooltips = new HashMap<>();

    public SearchBoxModeSideButtonWidget(TwoWaySyncProperty<GridSearchBoxMode> searchBoxModeProperty, TooltipRenderer tooltipRenderer) {
        super(createPressAction(searchBoxModeProperty));
        this.searchBoxModeProperty = searchBoxModeProperty;
        this.tooltipRenderer = tooltipRenderer;
    }

    private List<Text> calculateTooltip(GridSearchBoxMode searchBoxMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(new TranslatableText("gui.refinedstorage2.grid.search_box_mode"));
        lines.add(searchBoxMode.getDisplayProperties().getName());
        return lines;
    }

    private static PressAction createPressAction(TwoWaySyncProperty<GridSearchBoxMode> searchBoxModeProperty) {
        return btn -> searchBoxModeProperty.syncToServer(RefinedStorage2Mod.API.getGridSearchBoxModeRegistry().next(searchBoxModeProperty.getDeserialized()));
    }

    @Override
    protected Identifier getSpriteIdentifier() {
        return searchBoxModeProperty.getDeserialized().getDisplayProperties().getSpriteIdentifier();
    }

    @Override
    protected int getXTexture() {
        return searchBoxModeProperty.getDeserialized().getDisplayProperties().getX();
    }

    @Override
    protected int getYTexture() {
        return searchBoxModeProperty.getDeserialized().getDisplayProperties().getY();
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.computeIfAbsent(searchBoxModeProperty.getDeserialized(), this::calculateTooltip), mouseX, mouseY);
    }
}
