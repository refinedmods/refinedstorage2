package com.refinedmods.refinedstorage.platform.common.support.widget;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;

import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public class FuzzyModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "fuzzy_mode");
    private static final MutableComponent SUBTEXT_ON = createTranslation("gui", "fuzzy_mode.on");
    private static final MutableComponent SUBTEXT_OFF = createTranslation("gui", "fuzzy_mode.off");

    private final ClientProperty<Boolean> property;
    private final Supplier<Type> typeSupplier;

    public FuzzyModeSideButtonWidget(final ClientProperty<Boolean> property, final Supplier<Type> typeSupplier) {
        super(createPressAction(property));
        this.property = property;
        this.typeSupplier = typeSupplier;
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
    protected Component getHelpText() {
        final Type type = typeSupplier.get();
        return Boolean.TRUE.equals(property.getValue()) ? type.helpOnText : type.helpOffText;
    }

    public enum Type {
        STORAGE,
        GENERIC,
        EXTRACTING_STORAGE_NETWORK,
        EXTRACTING_SOURCE;

        private final MutableComponent helpOnText;
        private final MutableComponent helpOffText;

        Type() {
            this.helpOnText = createTranslation("gui", "fuzzy_mode.on." + getHelpTranslationKey());
            this.helpOffText = createTranslation("gui", "fuzzy_mode.off." + getHelpTranslationKey());
        }

        private String getHelpTranslationKey() {
            return name().toLowerCase() + "_help";
        }
    }
}
