package com.refinedmods.refinedstorage.common.support.widget;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.NO;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.YES;

public abstract class AbstractYesNoSideButtonWidget extends AbstractSideButtonWidget {
    private static final List<MutableComponent> YES_LINES = List.of(YES.copy().withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> NO_LINES = List.of(NO.copy().withStyle(ChatFormatting.GRAY));

    private final ClientProperty<Boolean> property;
    private final MutableComponent title;
    private final Identifier yesSprite;
    private final Identifier noSprite;

    protected AbstractYesNoSideButtonWidget(final ClientProperty<Boolean> property,
                                            final MutableComponent title,
                                            final Identifier yesSprite,
                                            final Identifier noSprite) {
        super(createPressAction(property));
        this.property = property;
        this.title = title;
        this.yesSprite = yesSprite;
        this.noSprite = noSprite;
    }

    private static OnPress createPressAction(final ClientProperty<Boolean> property) {
        return btn -> property.setValue(!property.getValue());
    }

    @Override
    protected Identifier getSprite() {
        return Boolean.TRUE.equals(property.getValue()) ? yesSprite : noSprite;
    }

    @Override
    protected MutableComponent getTitle() {
        return title;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return Boolean.TRUE.equals(property.getValue()) ? YES_LINES : NO_LINES;
    }
}
