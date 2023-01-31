package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.GridSize;
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

public class SizeSideButtonWidget extends AbstractSideButtonWidget {
    private final GridContainerMenu menu;
    private final TooltipRenderer tooltipRenderer;
    private final Map<GridSize, List<Component>> tooltips = new EnumMap<>(GridSize.class);

    public SizeSideButtonWidget(final GridContainerMenu menu, final TooltipRenderer tooltipRenderer) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(GridSize.values()).forEach(type -> tooltips.put(type, calculateTooltip(type)));
    }

    private static OnPress createPressAction(final GridContainerMenu menu) {
        return btn -> menu.setSize(menu.getSize().toggle());
    }

    private List<Component> calculateTooltip(final GridSize size) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.size"));
        lines.add(createTranslation("gui", "grid.size." + size.toString().toLowerCase(Locale.ROOT))
            .withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return switch (menu.getSize()) {
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
    public void onTooltip(final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(menu.getSize()), mouseX, mouseY);
    }
}
