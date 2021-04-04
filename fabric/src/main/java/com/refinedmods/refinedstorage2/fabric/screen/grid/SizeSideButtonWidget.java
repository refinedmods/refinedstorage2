package com.refinedmods.refinedstorage2.fabric.screen.grid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.refinedmods.refinedstorage2.core.grid.GridSize;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.property.TwoWaySyncProperty;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

public class SizeSideButtonWidget extends SideButtonWidget {
    private final TwoWaySyncProperty<GridSize> sizeProperty;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSize, List<Text>> tooltips = new EnumMap<>(GridSize.class);

    public SizeSideButtonWidget(TwoWaySyncProperty<GridSize> sizeProperty, TooltipRenderer tooltipRenderer) {
        super(createPressAction(sizeProperty));
        this.sizeProperty = sizeProperty;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSize.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private List<Text> calculateTooltip(GridSize size) {
        List<Text> lines = new ArrayList<>();
        lines.add(new TranslatableText("gui.refinedstorage2.grid.size"));
        lines.add(new TranslatableText("gui.refinedstorage2.grid.size." + size.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        return lines;
    }

    private static PressAction createPressAction(TwoWaySyncProperty<GridSize> sizeProperty) {
        return btn -> sizeProperty.syncToServer(sizeProperty.getDeserialized().toggle());
    }

    @Override
    protected int getXTexture() {
        switch (sizeProperty.getDeserialized()) {
            case STRETCH:
                return 64 + 48;
            case SMALL:
                return 64;
            case MEDIUM:
                return 64 + 16;
            case LARGE:
                return 64 + 32;
            default:
                return 64 + 48;
        }
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.get(sizeProperty.getDeserialized()), mouseX, mouseY);
    }
}
