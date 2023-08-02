package com.refinedmods.refinedstorage2.platform.common.screen.widget;

import com.refinedmods.refinedstorage2.platform.common.containermenu.property.ClientProperty;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class FuzzyModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "fuzzy_mode");
    private static final MutableComponent SUBTEXT_ON = createTranslation("gui", "fuzzy_mode.on");
    private static final MutableComponent SUBTEXT_OFF = createTranslation("gui", "fuzzy_mode.off");

    private final ClientProperty<Boolean> property;
    private final List<Component> helpOn;
    private final List<Component> helpOff;


    public FuzzyModeSideButtonWidget(final ClientProperty<Boolean> property, final Type type) {
        super(createPressAction(property));
        this.property = property;
        this.helpOn = List.of(createTranslation("gui", "fuzzy_mode.on." + type.getHelpTranslationKey()));
        this.helpOff = List.of(createTranslation("gui", "fuzzy_mode.off." + type.getHelpTranslationKey()));
    }

    private static OnPress createPressAction(final ClientProperty<Boolean> property) {
        return btn -> property.setValue(!property.getValue());
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 192;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return Boolean.TRUE.equals(property.getValue()) ? SUBTEXT_ON : SUBTEXT_OFF;
    }

    @Override
    protected List<Component> getHelpText() {
        return Boolean.TRUE.equals(property.getValue()) ? helpOn : helpOff;
    }

    public enum Type {
        STORAGE,
        DETECTOR,
        EXTRACTING_STORAGE_NETWORK,
        EXTRACTING_SOURCE;

        String getHelpTranslationKey() {
            return name().toLowerCase() + "_help";
        }
    }
}
