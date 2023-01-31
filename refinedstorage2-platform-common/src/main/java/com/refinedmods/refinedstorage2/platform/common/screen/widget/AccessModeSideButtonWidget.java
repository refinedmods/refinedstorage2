package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.api.storage.AccessMode;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

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

public class AccessModeSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<AccessMode> property;
    private final TooltipRenderer tooltipRenderer;
    private final Map<AccessMode, List<Component>> tooltips = new EnumMap<>(AccessMode.class);

    public AccessModeSideButtonWidget(final ClientProperty<AccessMode> property,
                                      final TooltipRenderer tooltipRenderer) {
        super(createPressAction(property));
        this.property = property;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(AccessMode.values()).forEach(accessMode ->
            tooltips.put(accessMode, calculateTooltip(accessMode)));
    }

    private static OnPress createPressAction(final ClientProperty<AccessMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static AccessMode toggle(final AccessMode accessMode) {
        return switch (accessMode) {
            case INSERT_EXTRACT -> AccessMode.INSERT;
            case INSERT -> AccessMode.EXTRACT;
            case EXTRACT -> AccessMode.INSERT_EXTRACT;
        };
    }

    private List<Component> calculateTooltip(final AccessMode accessMode) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "access_mode"));
        lines.add(createTranslation(
            "gui",
            "access_mode." + accessMode.toString().toLowerCase(Locale.ROOT)
        ).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return switch (property.getValue()) {
            case INSERT_EXTRACT -> 0;
            case INSERT -> 16;
            case EXTRACT -> 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 240;
    }

    @Override
    public void onTooltip(final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(property.getValue()), mouseX, mouseY);
    }
}
