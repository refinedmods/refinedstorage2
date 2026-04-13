package com.refinedmods.refinedstorage.common.autocrafting.autocraftermanager;

import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class ViewTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "autocrafter_manager.view_type");
    private static final List<MutableComponent> SUBTEXT_VISIBLE = List.of(
        createTranslation("gui", "autocrafter_manager.view_type.visible").withStyle(ChatFormatting.GRAY)
    );
    private static final MutableComponent HELP_VISIBLE =
        createTranslation("gui", "autocrafter_manager.view_type.visible.help");
    private static final List<MutableComponent> SUBTEXT_NOT_FULL = List.of(
        createTranslation("gui", "autocrafter_manager.view_type.not_full").withStyle(ChatFormatting.GRAY)
    );
    private static final MutableComponent HELP_NOT_FULL =
        createTranslation("gui", "autocrafter_manager.view_type.not_full.help");
    private static final List<MutableComponent> SUBTEXT_ALL = List.of(
        createTranslation("gui", "autocrafter_manager.view_type.all").withStyle(ChatFormatting.GRAY)
    );
    private static final MutableComponent HELP_ALL =
        createTranslation("gui", "autocrafter_manager.view_type.all.help");
    private static final Identifier SPRITE_VISIBLE =
        createIdentifier("widget/side_button/autocrafter_manager/view_type/visible");
    private static final Identifier SPRITE_NOT_FULL =
        createIdentifier("widget/side_button/autocrafter_manager/view_type/not_full");
    private static final Identifier SPRITE_ALL =
        createIdentifier("widget/side_button/autocrafter_manager/view_type/all");

    private final AutocrafterManagerContainerMenu containerMenu;

    ViewTypeSideButtonWidget(final AutocrafterManagerContainerMenu containerMenu) {
        super(createPressAction(containerMenu));
        this.containerMenu = containerMenu;
    }

    private static OnPress createPressAction(final AutocrafterManagerContainerMenu containerMenu) {
        return btn -> containerMenu.setViewType(containerMenu.getViewType().toggle());
    }

    @Override
    protected Identifier getSprite() {
        return switch (containerMenu.getViewType()) {
            case VISIBLE -> SPRITE_VISIBLE;
            case NOT_FULL -> SPRITE_NOT_FULL;
            case ALL -> SPRITE_ALL;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        return switch (containerMenu.getViewType()) {
            case VISIBLE -> SUBTEXT_VISIBLE;
            case NOT_FULL -> SUBTEXT_NOT_FULL;
            case ALL -> SUBTEXT_ALL;
        };
    }

    @Override
    protected Component getHelpText() {
        return switch (containerMenu.getViewType()) {
            case VISIBLE -> HELP_VISIBLE;
            case NOT_FULL -> HELP_NOT_FULL;
            case ALL -> HELP_ALL;
        };
    }
}
