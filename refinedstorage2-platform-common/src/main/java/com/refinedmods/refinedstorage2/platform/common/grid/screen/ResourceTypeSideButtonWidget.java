package com.refinedmods.refinedstorage2.platform.common.grid.screen;

import com.refinedmods.refinedstorage2.platform.api.support.resource.ResourceType;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

class ResourceTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.resource_type");
    private static final MutableComponent SUBTEXT_ALL = createTranslation("gui", "grid.resource_type.all");
    private static final Component HELP = createTranslation("gui", "grid.resource_type.help");

    private final AbstractGridContainerMenu menu;

    ResourceTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.toggleResourceType();
    }

    @Override
    protected ResourceLocation getTextureIdentifier() {
        final ResourceType resourceType = menu.getResourceType();
        if (resourceType == null) {
            return TextureIds.ICONS;
        }
        return resourceType.getTextureIdentifier();
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        final ResourceType resourceType = menu.getResourceType();
        if (resourceType == null) {
            return SUBTEXT_ALL;
        }
        return resourceType.getTitle();
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }

    @Override
    protected int getXTexture() {
        final ResourceType resourceType = menu.getResourceType();
        if (resourceType == null) {
            return 32;
        }
        return resourceType.getXTexture();
    }

    @Override
    protected int getYTexture() {
        final ResourceType resourceType = menu.getResourceType();
        if (resourceType == null) {
            return 128;
        }
        return resourceType.getYTexture();
    }
}
