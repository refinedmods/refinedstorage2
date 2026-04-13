package com.refinedmods.refinedstorage.common.storage.diskinterface;

import com.refinedmods.refinedstorage.api.network.impl.node.storagetransfer.StorageTransferMode;
import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractSideButtonWidget;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

class TransferModeSideButtonWidget extends AbstractSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "disk_interface.transfer_mode");
    private static final List<MutableComponent> SUBTEXT_INSERT_INTO_NETWORK = List.of(
        createTranslation("gui", "disk_interface.transfer_mode.insert_into_network").withStyle(ChatFormatting.GRAY)
    );
    private static final List<MutableComponent> SUBTEXT_EXTRACT_FROM_NETWORK = List.of(
        createTranslation("gui", "disk_interface.transfer_mode.extract_from_network").withStyle(ChatFormatting.GRAY)
    );
    private static final Component HELP_INSERT_INTO_NETWORK =
        createTranslation("gui", "disk_interface.transfer_mode.insert_into_network.help");
    private static final Component HELP_EXTRACT_FROM_NETWORK =
        createTranslation("gui", "disk_interface.transfer_mode.extract_from_network.help");
    private static final Identifier INSERT_INTO_NETWORK =
        createIdentifier("widget/side_button/disk_interface_transfer_mode/insert_into_network");
    private static final Identifier EXTRACT_FROM_NETWORK =
        createIdentifier("widget/side_button/disk_interface_transfer_mode/extract_from_network");

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
    protected Identifier getSprite() {
        return switch (property.getValue()) {
            case INSERT_INTO_NETWORK -> INSERT_INTO_NETWORK;
            case EXTRACT_FROM_NETWORK -> EXTRACT_FROM_NETWORK;
        };
    }

    @Override
    protected MutableComponent getTitle() {
        return TITLE;
    }

    @Override
    protected List<MutableComponent> getSubText() {
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
