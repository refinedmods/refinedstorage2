package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.block.entity.SchedulingModeType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import java.util.List;

import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

// TODO: Textures!
public class SchedulingModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "scheduling_mode");

    private final ClientProperty<SchedulingModeType> property;

    public SchedulingModeSideButtonWidget(final ClientProperty<SchedulingModeType> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<SchedulingModeType> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static SchedulingModeType toggle(final SchedulingModeType modeSettings) {
        return switch (modeSettings) {
            case DEFAULT -> SchedulingModeType.ROUND_ROBIN;
            case ROUND_ROBIN -> SchedulingModeType.RANDOM;
            case RANDOM -> SchedulingModeType.DEFAULT;
        };
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
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return property.getValue().getName();
    }

    @Override
    protected List<MutableComponent> getHelpText() {
        return property.getValue().getHelp();
    }
}
