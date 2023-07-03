package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.block.entity.exporter.ExporterSchedulingModeSettings;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO: Textures!
public class ExporterSchedulingModeSideButtonWidget extends AbstractSideButtonWidget {
    private final ClientProperty<ExporterSchedulingModeSettings> property;

    public ExporterSchedulingModeSideButtonWidget(final ClientProperty<ExporterSchedulingModeSettings> property) {
        super(createPressAction(property));
        this.property = property;
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
    protected List<Component> getSideButtonTooltip() {
        return calculateTooltip(property.getValue());
    }
}
