package com.refinedmods.refinedstorage2.platform.fabric.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingDirection;
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

public class SortingDirectionSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSortingDirection, List<Component>> tooltips = new EnumMap<>(GridSortingDirection.class);

    public SortingDirectionSideButtonWidget(GridContainerMenu screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSortingDirection.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(GridContainerMenu screenHandler) {
        return btn -> screenHandler.setSortingDirection(screenHandler.getSortingDirection().toggle());
    }

    private List<Component> calculateTooltip(GridSortingDirection type) {
        List<Component> lines = new ArrayList<>();
        lines.add(Rs2Mod.createTranslation("gui", "grid.sorting.direction"));
        lines.add(Rs2Mod.createTranslation("gui", "grid.sorting.direction." + type.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return screenHandler.getSortingDirection() == GridSortingDirection.ASCENDING ? 0 : 16;
    }

    @Override
    protected int getYTexture() {
        return 16;
    }

    @Override
    public void onTooltip(Button buttonWidget, PoseStack matrixStack, int mouseX, int mouseY) {
        tooltipRenderer.render(matrixStack, tooltips.get(screenHandler.getSortingDirection()), mouseX, mouseY);
    }
}
