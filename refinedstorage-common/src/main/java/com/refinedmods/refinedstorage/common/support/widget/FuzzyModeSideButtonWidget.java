package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class FuzzyModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "fuzzy_mode");
    private static final List<MutableComponent> SUBTEXT_ON = List.of(
        createTranslation("gui", "fuzzy_mode.on").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_OFF = List.of(
        createTranslation("gui", "fuzzy_mode.off").withStyle(ChatFormatting.GRAY)
    );
    private static final Identifier ON = createIdentifier("widget/side_button/fuzzy_mode/on");
    private static final Identifier OFF = createIdentifier("widget/side_button/fuzzy_mode/off");

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
    protected Identifier getSprite() {
        return Boolean.TRUE.equals(property.getValue()) ? ON : OFF;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
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
