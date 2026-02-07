package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.api.storage.AccessMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class AccessModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "access_mode");
    private static final List<MutableComponent> SUBTEXT_INSERT =
        List.of(createTranslation("gui", "access_mode.insert").withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> SUBTEXT_EXTRACT =
        List.of(createTranslation("gui", "access_mode.extract").withStyle(ChatFormatting.GRAY));
    private static final List<MutableComponent> SUBTEXT_INSERT_EXTRACT =
        List.of(createTranslation("gui", "access_mode.insert_extract").withStyle(ChatFormatting.GRAY));
    private static final Component HELP_INSERT =
        createTranslation("gui", "access_mode.insert.help");
    private static final Component HELP_EXTRACT =
        createTranslation("gui", "access_mode.extract.help");
    private static final Component HELP_INSERT_EXTRACT =
        createTranslation("gui", "access_mode.insert_extract.help");
    private static final Identifier INSERT = createIdentifier("widget/side_button/storage/access_mode/insert");
    private static final Identifier EXTRACT = createIdentifier("widget/side_button/storage/access_mode/extract");
    private static final Identifier INSERT_EXTRACT =
        createIdentifier("widget/side_button/storage/access_mode/insert_extract");

    private final ClientProperty<AccessMode> property;

    public AccessModeSideButtonWidget(final ClientProperty<AccessMode> property) {
        super(createPressAction(property));
        this.property = property;
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

    @Override
    protected Identifier getSprite() {
        return switch (property.getValue()) {
            case INSERT_EXTRACT -> INSERT_EXTRACT;
            case INSERT -> INSERT;
            case EXTRACT -> EXTRACT;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (property.getValue()) {
            case INSERT_EXTRACT -> SUBTEXT_INSERT_EXTRACT;
            case INSERT -> SUBTEXT_INSERT;
            case EXTRACT -> SUBTEXT_EXTRACT;
        };
    }

    @Override
    protected Component getHelpText() {
        return switch (property.getValue()) {
            case INSERT_EXTRACT -> HELP_INSERT_EXTRACT;
            case INSERT -> HELP_INSERT;
            case EXTRACT -> HELP_EXTRACT;
        };
    }
}
