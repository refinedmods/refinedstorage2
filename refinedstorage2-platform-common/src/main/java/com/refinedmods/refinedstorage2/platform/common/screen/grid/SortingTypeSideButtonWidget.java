package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.SideButtonWidget;

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

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SortingTypeSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu screenHandler;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSortingType, List<Component>> tooltips = new EnumMap<>(GridSortingType.class);

    public SortingTypeSideButtonWidget(GridContainerMenu screenHandler, TooltipRenderer tooltipRenderer) {
        super(createPressAction(screenHandler));
        this.screenHandler = screenHandler;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSortingType.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(GridContainerMenu screenHandler) {
        return btn -> screenHandler.setSortingType(screenHandler.getSortingType().toggle());
    }

    private List<Component> calculateTooltip(GridSortingType type) {
        List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.sorting.type"));
        lines.add(createTranslation("gui", "grid.sorting.type." + type.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return switch (screenHandler.getSortingType()) {
            case QUANTITY -> 0;
            case NAME -> 16;
            case ID -> 32;
            case LAST_MODIFIED -> 48;
        };
    }

    @Override
    protected int getYTexture() {
        return screenHandler.getSortingType() == GridSortingType.LAST_MODIFIED ? 48 : 32;
    }

    @Override
    public void onTooltip(Button buttonWidget, PoseStack poseStack, int mouseX, int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(screenHandler.getSortingType()), mouseX, mouseY);
    }
}
