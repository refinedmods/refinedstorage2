package com.refinedmods.refinedstorage2.platform.fabric.screen.widget;

import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.screenhandler.ExactModeAccessor;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class ExactModeSideButtonWidget extends SideButtonWidget {
    private final ExactModeAccessor exactModeAccessor;
    private final TooltipRenderer tooltipRenderer;
    private final List<Component> onTooltip;
    private final List<Component> offTooltip;

    public ExactModeSideButtonWidget(ExactModeAccessor exactModeAccessor, TooltipRenderer tooltipRenderer) {
        super(createPressAction(exactModeAccessor));
        this.exactModeAccessor = exactModeAccessor;
        this.tooltipRenderer = tooltipRenderer;
        this.onTooltip = calculateTooltip(true);
        this.offTooltip = calculateTooltip(false);
    }

    private static OnPress createPressAction(ExactModeAccessor exactModeAccessor) {
        return btn -> exactModeAccessor.setExactMode(!exactModeAccessor.isExactMode());
    }

    private List<Component> calculateTooltip(boolean exactMode) {
        List<Component> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "exact_mode"));
        lines.add(Rs2Mod.createTranslation("gui", "exact_mode." + (exactMode ? "on" : "off")).withStyle(ChatFormatting.GRAY));
        return lines;
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
    public void onTooltip(Button button, PoseStack matrices, int mouseX, int mouseY) {
        tooltipRenderer.render(matrices, exactModeAccessor.isExactMode() ? onTooltip : offTooltip, mouseX, mouseY);
    }
}
