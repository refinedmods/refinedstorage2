package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridSize;
import com.refinedmods.refinedstorage2.platform.fabric.Rs2Mod;
import com.refinedmods.refinedstorage2.platform.fabric.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.fabric.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.fabric.screen.widget.SideButtonWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class SizeSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSize, List<Component>> tooltips = new EnumMap<>(GridSize.class);

    public SizeSideButtonWidget(GridContainerMenu screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSize.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(GridContainerMenu screenHandler) {
        return btn -> screenHandler.setSize(screenHandler.getSize().toggle());
    }

    private List<Component> calculateTooltip(GridSize size) {
        List<Component> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "grid.size"));
        lines.add(Rs2Mod.createTranslation("gui", "grid.size." + size.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
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
    public void onTooltip(Button buttonWidget, PoseStack poseStack, int mouseX, int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(screenHandler.getSize()), mouseX, mouseY);
    }
}
