package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterSchedulingModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.screen.TooltipRenderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class ExporterSchedulingModeSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<ExporterSchedulingModeSettings> property;
    private final TooltipRenderer tooltipRenderer;

    public ExporterSchedulingModeSideButtonWidget(final ClientProperty<ExporterSchedulingModeSettings> property,
                                                  final TooltipRenderer tooltipRenderer) {
        super(createPressAction(property));
        this.property = property;
        this.tooltipRenderer = tooltipRenderer;
    }

    private static OnPress createPressAction(final ClientProperty<ExporterSchedulingModeSettings> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static ExporterSchedulingModeSettings toggle(final ExporterSchedulingModeSettings modeSettings) {
        return switch (modeSettings) {
            case FIRST_AVAILABLE -> ExporterSchedulingModeSettings.ROUND_ROBIN;
            case ROUND_ROBIN -> ExporterSchedulingModeSettings.RANDOM;
            case RANDOM -> ExporterSchedulingModeSettings.FIRST_AVAILABLE;
        };
    }

    private List<Component> calculateTooltip(final ExporterSchedulingModeSettings modeSettings) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "exporter.scheduling_mode"));
        lines.add(modeSettings.getName().withStyle(ChatFormatting.GRAY));
        return lines;
    }

    @Override
    protected int getXTexture() {
        return -16;
    }

    @Override
    protected int getYTexture() {
        return -16;
    }

    @Override
    public void onTooltip(final Button button, final PoseStack poseStack, final int mouseX, final int mouseY) {
        tooltipRenderer.render(
            poseStack,
            calculateTooltip(property.getValue()),
            mouseX,
            mouseY
        );
    }
}
