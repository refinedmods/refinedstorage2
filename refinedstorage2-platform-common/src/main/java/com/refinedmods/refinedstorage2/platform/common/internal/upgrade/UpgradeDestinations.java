package com.refinedmods.refinedstorage2.platform.common.internal.upgrade;

import com.refinedmods.refinedstorage2.platform.api.upgrade.UpgradeDestination;
import com.refinedmods.refinedstorage2.platform.common.content.ContentIds;

import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public enum UpgradeDestinations implements UpgradeDestination {
    IMPORTER(createTranslation("block", ContentIds.IMPORTER.getPath())),
    EXPORTER(createTranslation("block", ContentIds.EXPORTER.getPath())),
    INTERFACE(createTranslation("block", ContentIds.INTERFACE.getPath()));

    private final Component name;

    UpgradeDestinations(final Component name) {
        this.name = name;
    }

    @Override
    public Component getName() {
        return name;
    }
}
