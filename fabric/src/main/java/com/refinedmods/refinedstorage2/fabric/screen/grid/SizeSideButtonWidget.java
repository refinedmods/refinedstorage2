package com.refinedmods.refinedstorage2.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.GridSize;
import com.refinedmods.refinedstorage2.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screen.widget.SideButtonWidget;
import com.refinedmods.refinedstorage2.fabric.screenhandler.grid.GridScreenHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SizeSideButtonWidget extends SideButtonWidget {
    private final GridScreenHandler screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSize, List<Text>> tooltips = new EnumMap<>(GridSize.class);

    public SizeSideButtonWidget(GridScreenHandler screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSize.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static PressAction createPressAction(GridScreenHandler screenHandler) {
        return btn -> screenHandler.setSize(screenHandler.getSize().toggle());
    }

    private List<Text> calculateTooltip(GridSize size) {
        List<Text> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "grid.size"));
        lines.add(Rs2Mod.createTranslation("gui", "grid.size." + size.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return switch (screenHandler.getSize()) {
            case STRETCH -> 64 + 48;
            case SMALL -> 64;
            case MEDIUM -> 64 + 16;
            case LARGE, EXTRA_LARGE -> 64 + 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    public void onTooltip(ButtonWidget buttonWidget, MatrixStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.get(screenHandler.getSize()), mouseX, mouseY);
    }
}
