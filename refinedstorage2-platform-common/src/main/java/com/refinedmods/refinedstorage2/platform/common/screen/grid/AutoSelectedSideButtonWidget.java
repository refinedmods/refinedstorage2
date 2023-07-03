package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class AutoSelectedSideButtonWidget extends AbstractSideButtonWidget {
    private final AbstractGridContainerMenu menu;
    private final List<Component> yes;
    private final List<Component> no;

    public AutoSelectedSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
        this.yes = calculateTooltip(true);
        this.no = calculateTooltip(false);
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.setAutoSelected(!menu.isAutoSelected());
    }

    private List<Component> calculateTooltip(final boolean autoSelected) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.auto_selected"));
        lines.add(Component.translatable("gui." + (autoSelected ? "yes" : "no")).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return menu.isAutoSelected() ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 96;
    }

    @Override
    protected List<Component> getSideButtonTooltip() {
        return menu.isAutoSelected() ? yes : no;
    }
}
