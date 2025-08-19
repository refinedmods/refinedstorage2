package com.refinedmods.refinedstorage.common.grid.screen;

import com.refinedmods.refinedstorage.common.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class ResourceTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.resource_type");
    private static final MutableComponent EMPTY_WARNING = createTranslation("gui", "grid.resource_type.empty_warning");
    private static final List<MutableComponent> SUBTEXT_ALL =
        List.of(createTranslation("gui", "grid.resource_type.all").withStyle(ChatFormatting.GRAY));
    private static final ResourceLocation ALL = createIdentifier("widget/side_button/resource_type/all");

    private final AbstractGridContainerMenu menu;

    ResourceTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.toggleResourceType();
    }

    @Override
    protected ResourceLocation getSprite() {
        final ResourceType resourceType = menu.getResourceType();
        if (resourceType == null) {
            return ALL;
        }
        return resourceType.getSprite();
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
        final ResourceType resourceType = menu.getResourceType();
        if (resourceType == null) {
            return SUBTEXT_ALL;
        }
        return List.of(resourceType.getTitle().withStyle(ChatFormatting.GRAY));
    }

    public void setWarningVisible(final boolean visible) {
        if (visible) {
            setWarning(EMPTY_WARNING);
        } else {
            setWarning(null);
        }
    }
}
