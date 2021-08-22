package com.refinedmods.refinedstorage2.platform.fabric.screen.widget;

import com.refinedmods.refinedstorage2.api.stack.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.FilterModeAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class FilterModeSideButtonWidget extends SideButtonWidget {
    private final FilterModeAccessor filterModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final List<Text> blockModeTooltip;
    private final List<Text> allowModeTooltip;

    public FilterModeSideButtonWidget(FilterModeAccessor filterModeAccessor, TooltipRenderer tooltipRenderer) {
        super(createPressAction(filterModeAccessor));
        this.filterModeAccessor = filterModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        this.blockModeTooltip = calculateTooltip(FilterMode.BLOCK);
        this.allowModeTooltip = calculateTooltip(FilterMode.ALLOW);
    }

    private static PressAction createPressAction(FilterModeAccessor filterModeAccessor) {
        return btn -> filterModeAccessor.setFilterMode(filterModeAccessor.getFilterMode().toggle());
    }

    private List<Text> calculateTooltip(FilterMode filterMode) {
        List<Text> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "filter_mode"));
        lines.add(Rs2Mod.createTranslation("gui", "filter_mode." + filterMode.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return filterModeAccessor.getFilterMode() == FilterMode.BLOCK ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 64;
    }

    @Override
    public void onTooltip(ButtonWidget button, MatrixStack matrices, int mouseX, int mouseY) {
        tooltipRenderer.render(matrices, filterModeAccessor.getFilterMode() == FilterMode.BLOCK ? blockModeTooltip : allowModeTooltip, mouseX, mouseY);
    }
}
