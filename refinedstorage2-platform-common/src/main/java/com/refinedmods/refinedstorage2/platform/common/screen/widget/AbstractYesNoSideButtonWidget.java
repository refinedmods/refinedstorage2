package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public abstract class AbstractYesNoSideButtonWidget extends AbstractSideButtonWidget {
    protected final ClientProperty<Boolean> property;
    private final List<Component> yesTooltip;
    private final List<Component> noTooltip;
    private final Component title;

    protected AbstractYesNoSideButtonWidget(final ClientProperty<Boolean> property, final Component title) {
        super(createPressAction(property));
        this.property = property;
        this.title = title;
        this.yesTooltip = calculateTooltip(true);
        this.noTooltip = calculateTooltip(false);
    }

    private static OnPress createPressAction(final ClientProperty<Boolean> property) {
        return btn -> property.setValue(!property.getValue());
    }

    private List<Component> calculateTooltip(final boolean value) {
        final List<Component> lines = new ArrayList<>();
        lines.add(title);
        lines.add(Component.translatable(value ? "gui.yes" : "gui.no").withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected List<Component> getSideButtonTooltip() {
        return Boolean.TRUE.equals(property.getValue()) ? yesTooltip : noTooltip;
    }
}
