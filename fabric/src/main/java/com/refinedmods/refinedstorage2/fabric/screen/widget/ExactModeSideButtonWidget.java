package com.refinedmods.refinedstorage2.fabric.screen.widget;

import java.util.ArrayList;
import java.util.List;

import com.refinedmods.refinedstorage2.fabric.RefinedStorage2Mod;
import com.refinedmods.refinedstorage2.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.fabric.screenhandler.ExactModeAccessor;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ExactModeSideButtonWidget extends SideButtonWidget {
    private final ExactModeAccessor exactModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final List<Text> onTooltip;
    private final List<Text> offTooltip;

    public ExactModeSideButtonWidget(ExactModeAccessor exactModeAccessor, TooltipRenderer tooltipRenderer) {
        super(createPressAction(exactModeAccessor));
        this.exactModeAccessor = exactModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        this.onTooltip = calculateTooltip(true);
        this.offTooltip = calculateTooltip(false);
    }

    private List<Text> calculateTooltip(boolean exactMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(RefinedStorage2Mod.createTranslation("gui", "exact_mode"));
        lines.add(RefinedStorage2Mod.createTranslation("gui", "exact_mode." + (exactMode ? "on" : "off")).formatted(Formatting.GRAY));
        return lines;
    }

    private static PressAction createPressAction(ExactModeAccessor exactModeAccessor) {
        return btn -> exactModeAccessor.setExactMode(!exactModeAccessor.isExactMode());
    }

    @Override
    protected int getXTexture() {
        return exactModeAccessor.isExactMode() ? 0 : 16;
    }

    @Override
    protected int getYTexture() {
        return 192;
    }

    @Override
    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        tooltipRenderer.render(matrices, exactModeAccessor.isExactMode() ? onTooltip : offTooltip, mouseX, mouseY);
    }
}
