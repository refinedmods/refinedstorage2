package com.refinedmods.refinedstorage2.platform.common.screen.grid;

import com.refinedmods.refinedstorage2.platform.api.storage.channel.PlatformStorageChannelType;
import com.refinedmods.refinedstorage2.platform.common.containermenu.grid.AbstractGridContainerMenu;
import com.refinedmods.refinedstorage2.platform.common.screen.TextureIds;
import com.refinedmods.refinedstorage2.platform.common.screen.widget.AbstractSideButtonWidget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class StorageChannelTypeSideButtonWidget extends AbstractSideButtonWidget {
    private static final List<Component> ALL_TOOLTIP = calculateTooltip(null);

    private final AbstractGridContainerMenu menu;
    private final Map<PlatformStorageChannelType<?>, List<Component>> tooltips = new HashMap<>();

    public StorageChannelTypeSideButtonWidget(final AbstractGridContainerMenu menu,
                                              final List<PlatformStorageChannelType<?>> storageChannelTypes) {
        super(createPressAction(menu));
        this.menu = menu;
        storageChannelTypes.forEach(
            storageChannelType -> tooltips.put(storageChannelType, calculateTooltip(storageChannelType))
        );
    }

    private static OnPress createPressAction(final AbstractGridContainerMenu menu) {
        return btn -> menu.toggleStorageChannelType();
    }

    private static List<Component> calculateTooltip(@Nullable final PlatformStorageChannelType<?> storageChannelType) {
        final List<Component> lines = new ArrayList<>();
        lines.add(createTranslation("gui", "grid.storage_channel_type"));
        if (storageChannelType != null) {
            lines.add(storageChannelType.getTitle().withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(createTranslation("gui", "grid.storage_channel_type.all").withStyle(ChatFormatting.GRAY));
        }
        return lines;
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

    @Override
    protected List<Component> getSideButtonTooltip() {
        final PlatformStorageChannelType<?> storageChannelType = menu.getStorageChannelType();
        if (storageChannelType == null) {
            return ALL_TOOLTIP;
        }
        return tooltips.get(storageChannelType);
    }
}
