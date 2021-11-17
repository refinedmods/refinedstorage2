package com.refinedmods.refinedstorage2.platform.fabric.screen.widget;

import com.refinedmods.refinedstorage2.api.core.filter.FilterMode;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.FilterModeAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class FilterModeSideButtonWidget extends SideButtonWidget {
    private final FilterModeAccessor filterModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> blockModeTooltip;
    private final List<Component> allowModeTooltip;

    public FilterModeSideButtonWidget(FilterModeAccessor filterModeAccessor, TooltipRenderer tooltipRenderer) {
        super(createPressAction(filterModeAccessor));
        this.filterModeAccessor = filterModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        this.blockModeTooltip = calculateTooltip(FilterMode.BLOCK);
        this.allowModeTooltip = calculateTooltip(FilterMode.ALLOW);
    }

    private static OnPress createPressAction(FilterModeAccessor filterModeAccessor) {
        return btn -> filterModeAccessor.setFilterMode(filterModeAccessor.getFilterMode().toggle());
    }

    private List<Component> calculateTooltip(FilterMode filterMode) {
        List<Component> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "filter_mode"));
        lines.add(Rs2Mod.createTranslation("gui", "filter_mode." + filterMode.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
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
    public void onTooltip(Button button, PoseStack matrices, int mouseX, int mouseY) {
        tooltipRenderer.render(matrices, filterModeAccessor.getFilterMode() == FilterMode.BLOCK ? blockModeTooltip : allowModeTooltip, mouseX, mouseY);
    }
}
