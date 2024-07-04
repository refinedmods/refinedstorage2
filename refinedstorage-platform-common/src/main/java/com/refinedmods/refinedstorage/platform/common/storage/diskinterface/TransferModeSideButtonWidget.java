package com.refinedmods.refinedstorage.platform.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.widget.AbstractSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

class TransferModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "disk_interface.transfer_mode");
    private static final MutableComponent SUBTEXT_INSERT_INTO_NETWORK =
        createTranslation("gui", "disk_interface.transfer_mode.insert_into_network");
    private static final MutableComponent SUBTEXT_EXTRACT_FROM_NETWORK =
        createTranslation("gui", "disk_interface.transfer_mode.extract_from_network");
    private static final Component HELP_INSERT_INTO_NETWORK =
        createTranslation("gui", "disk_interface.transfer_mode.insert_into_network.help");
    private static final Component HELP_EXTRACT_FROM_NETWORK =
        createTranslation("gui", "disk_interface.transfer_mode.extract_from_network.help");

    private final ClientProperty<StorageTransferMode> property;

    TransferModeSideButtonWidget(final ClientProperty<StorageTransferMode> property) {
        super(createPressAction(property));
        this.property = property;
    }

    private static OnPress createPressAction(final ClientProperty<StorageTransferMode> property) {
        return btn -> property.setValue(toggle(property.getValue()));
    }

    private static StorageTransferMode toggle(final StorageTransferMode accessMode) {
        return switch (accessMode) {
            case INSERT_INTO_NETWORK -> StorageTransferMode.EXTRACT_FROM_NETWORK;
            case EXTRACT_FROM_NETWORK -> StorageTransferMode.INSERT_INTO_NETWORK;
        };
    }

    @Override
    protected int getXTexture() {
        return switch (property.getValue()) {
            case INSERT_INTO_NETWORK -> 16;
            case EXTRACT_FROM_NETWORK -> 0;
        };
    }

    @Override
    protected int getYTexture() {
        return 160;
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected MutableComponent getSubText() {
        return switch (property.getValue()) {
            case INSERT_INTO_NETWORK -> SUBTEXT_INSERT_INTO_NETWORK;
            case EXTRACT_FROM_NETWORK -> SUBTEXT_EXTRACT_FROM_NETWORK;
        };
    }

    @Override
    protected Component getHelpText() {
        return switch (property.getValue()) {
            case INSERT_INTO_NETWORK -> HELP_INSERT_INTO_NETWORK;
            case EXTRACT_FROM_NETWORK -> HELP_EXTRACT_FROM_NETWORK;
        };
    }
}
