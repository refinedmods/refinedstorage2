package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class StorageChannelTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "grid.storage_channel_type");
    private static final MutableComponent SUBTEXT_ALL = createTranslation("gui", "grid.storage_channel_type.all");
    private static final List<Component> HELP =
        List.of(createTranslation("gui", "grid.storage_channel_type.help"));

    private final AbstractGridContainerMenu menu;

    public StorageChannelTypeSideButtonWidget(final AbstractGridContainerMenu menu) {
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
    protected List<Component> getHelpText() {
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
