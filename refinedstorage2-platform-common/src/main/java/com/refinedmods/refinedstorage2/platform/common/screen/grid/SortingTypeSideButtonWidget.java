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
    private final GridContainerMenu<?> menu;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSortingType, List<Component>> tooltips = new EnumMap<>(GridSortingType.class);

    public SortingTypeSideButtonWidget(final GridContainerMenu<?> menu, final TooltipRenderer tooltipRenderer) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSortingType.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(final GridContainerMenu<?> menu) {
        return btn -> menu.setSortingType(menu.getSortingType().toggle());
    }

    private List<Component> calculateTooltip(final GridSortingType type) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.sorting.type"));
        lines.add(createTranslation("gui", "grid.sorting.type." + type.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return switch (menu.getSortingType()) {
            case QUANTITY -> 0;
            case NAME -> 16;
            case ID -> 32;
            case LAST_MODIFIED -> 48;
        };
    }

    @Override
    protected int getYTexture() {
        return menu.getSortingType() == GridSortingType.LAST_MODIFIED ? 48 : 32;
    }

    @Override
    public void onTooltip(final Button buttonWidget, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(menu.getSortingType()), mouseX, mouseY);
    }
}
