package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.api.network.impl.node.detector.DetectorMode;
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

public class DetectorModeSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<DetectorMode> property;
    private final TooltipRenderer tooltipRenderer;
    private final Map<DetectorMode, List<Component>> tooltips = new EnumMap<>(DetectorMode.class);

    public DetectorModeSideButtonWidget(final ClientProperty<DetectorMode> property,
                                        final TooltipRenderer tooltipRenderer) {
        super(createPressAction(property));
        this.property = property;
        this.tooltipRenderer = tooltipRenderer;
        Arrays.stream(DetectorMode.values()).forEach(detectorMode ->
            tooltips.put(detectorMode, calculateTooltip(detectorMode)));
    }

    private static OnPress createPressAction(final ClientProperty<DetectorMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static DetectorMode toggle(final DetectorMode detectorMode) {
        return switch (detectorMode) {
            case UNDER -> DetectorMode.EQUAL;
            case EQUAL -> DetectorMode.ABOVE;
            case ABOVE -> DetectorMode.UNDER;
        };
    }

    private List<Component> calculateTooltip(final DetectorMode mode) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "detector.mode"));
        lines.add(createTranslation(
            "gui",
            "detector.mode." + mode.toString().toLowerCase(Locale.ROOT)
        ).withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return switch (property.getValue()) {
            case UNDER -> 0;
            case EQUAL -> 16;
            case ABOVE -> 32;
        };
    }

    @Override
    protected int getYTexture() {
        return 176;
    }

    @Override
    public void onTooltip(final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(poseStack, tooltips.get(property.getValue()), mouseX, mouseY);
    }
}
