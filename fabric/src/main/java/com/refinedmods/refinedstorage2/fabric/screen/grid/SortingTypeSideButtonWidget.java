package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.grid.GridSortingType;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.handler.property.TwoWaySyncProperty;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class SortingTypeSideButtonWidget extends SideButtonWidget {
    private final TwoWaySyncProperty<GridSortingType> sortingTypeProperty;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSortingType, List<Text>> tooltips = new EnumMap<>(GridSortingType.class);

    public SortingTypeSideButtonWidget(TwoWaySyncProperty<GridSortingType> sortingTypeProperty, TooltipRenderer tooltipRenderer) {
        super(createPressAction(sortingTypeProperty));
        this.sortingTypeProperty = sortingTypeProperty;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSortingType.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private List<Text> calculateTooltip(GridSortingType type) {
        List<Text> lines = new ArrayList<>();
        lines.add(new TranslatableText("gui.refinedstorage2.grid.sorting.type"));
        lines.add(new TranslatableText("gui.refinedstorage2.grid.sorting.type." + type.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        return lines;
    }

    private static PressAction createPressAction(TwoWaySyncProperty<GridSortingType> sortingTypeProperty) {
        return btn -> sortingTypeProperty.syncToServer(sortingTypeProperty.getDeserialized().toggle());
    }

    @Override
    protected int getXTexture() {
        switch (sortingTypeProperty.getDeserialized()) {
            case QUANTITY:
                return 0;
            case NAME:
                return 16;
            case ID:
                return 32;
            case LAST_MODIFIED:
                return 48;
            default:
                return 0;
        }
    }

    @Override
    protected int getYTexture() {
        return sortingTypeProperty.getDeserialized() == GridSortingType.LAST_MODIFIED ? 48 : 32;
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.get(sortingTypeProperty.getDeserialized()), mouseX, mouseY);
    }
}
