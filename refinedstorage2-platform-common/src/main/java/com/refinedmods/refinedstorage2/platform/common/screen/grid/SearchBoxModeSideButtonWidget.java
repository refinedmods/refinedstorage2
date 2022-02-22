package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.api.grid.search.GridSearchBoxModeRegistry;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.GridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.internal.grid.search.PlatformSearchBoxModeImpl;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.SideButtonWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class SearchBoxModeSideButtonWidget extends SideButtonWidget {
    private final GridContainerMenu<?> menu;
    private final TooltipRenderer tooltipRenderer;
    private final Map<PlatformSearchBoxModeImpl, List<Component>> tooltips = new HashMap<>();

    public SearchBoxModeSideButtonWidget(GridContainerMenu<?> menu, TooltipRenderer tooltipRenderer) {
        super(createPressAction(menu));
        this.menu = menu;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static OnPress createPressAction(GridContainerMenu<?> menu) {
        return btn -> menu.setSearchBoxMode((PlatformSearchBoxModeImpl) GridSearchBoxModeRegistry.INSTANCE.next(menu.getSearchBoxMode()));
    }

    private List<Component> calculateTooltip(PlatformSearchBoxModeImpl searchBoxMode) {
        List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.search_box_mode"));
        lines.add(searchBoxMode.getName().withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected ResourceLocation getTextureIdentifier() {
        return menu.getSearchBoxMode().getTextureIdentifier();
    }

    @Override
    protected int getXTexture() {
        return menu.getSearchBoxMode().getTextureX();
    }

    @Override
    protected int getYTexture() {
        return menu.getSearchBoxMode().getTextureY();
    }

    @Override
    public void onTooltip(Button buttonWidget, PoseStack poseStack, int mouseX, int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.computeIfAbsent(menu.getSearchBoxMode(), this::calculateTooltip), mouseX, mouseY);
    }
}
