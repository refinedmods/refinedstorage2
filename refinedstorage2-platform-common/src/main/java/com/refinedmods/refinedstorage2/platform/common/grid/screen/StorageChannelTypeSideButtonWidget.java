package com.refinedmods.refinedstorage2.platform.common.grid.screen;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.support.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

class StorageChannelTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.storage_channel_type");
    private static final MutableComponent SUBTEXT_ALL = createTranslation("gui", "grid.storage_channel_type.all");
    private static final Component HELP = createTranslation("gui", "grid.storage_channel_type.help");

    private final AbstractGridContainerMenu menu;

    StorageChannelTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
        super(createPressAction(menu));
        this.menu = menu;
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.toggleStorageChannelType();
    }

    @Override
    protected ResourceLocation getTextureIdentifier() {
        final PlatformStorageChannelType<?> storageChannelType = menu.getStorageChannelType();
        if (storageChannelType == null) {
            return TextureIds.ICONS;
        }
        return storageChannelType.getTextureIdentifier();
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        final PlatformStorageChannelType<?> storageChannelType = menu.getStorageChannelType();
        if (storageChannelType == null) {
            return SUBTEXT_ALL;
        }
        return storageChannelType.getTitle();
    }

    @Override
    protected Component getHelpText() {
        return HELP;
    }

    @Override
    protected int getXTexture() {
        final PlatformStorageChannelType<?> storageChannelType = menu.getStorageChannelType();
        if (storageChannelType == null) {
            return 32;
        }
        return storageChannelType.getXTexture();
    }

    @Override
    protected int getYTexture() {
        final PlatformStorageChannelType<?> storageChannelType = menu.getStorageChannelType();
        if (storageChannelType == null) {
            return 128;
        }
        return storageChannelType.getYTexture();
    }
}
