package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.view.GridSortingType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SortingTypeSideButtonWidget extends AbstractSideButtonWidget {
    private final AbstractGridContainerMenu<?> menu;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSortingType, List<Component>> tooltips = new EnumMap<>(GridSortingType.class);

    public SortingTypeSideButtonWidget(final AbstractGridContainerMenu<?> menu, final TooltipRenderer tooltipRenderer) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSortingType.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu<?> menu) {
        return btn -> menu.setSortingType(toggle(menu.getSortingType()));
    }

    private static GridSortingType toggle(final GridSortingType sortingType) {
        return switch (sortingType) {
            case QUANTITY -> GridSortingType.NAME;
            case NAME -> GridSortingType.ID;
            case ID -> GridSortingType.LAST_MODIFIED;
            case LAST_MODIFIED -> GridSortingType.QUANTITY;
        };
    }

    private List<Component> calculateTooltip(final GridSortingType type) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.sorting.type"));
        lines.add(createTranslation(
            "gui",
            "grid.sorting.type." + type.toString().toLowerCase(Locale.ROOT)
        ).withStyle(ChatFormatting.GRAY));
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
    public void onTooltip(final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(menu.getSortingType()), mouseX, mouseY);
    }
}
