package com.refinedmods.refinedstorage.platform.common.networking;

import com.refinedmods.refinedstorage.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.platform.common.support.widget.AbstractYesNoSideButtonWidget;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

class RelayPassSecuritySideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final MutableComponent TITLE = createTranslation("gui", "relay.pass_security");
    private static final MutableComponent HELP = createTranslation("gui", "relay.pass_security.help");

    RelayPassSecuritySideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, TITLE);
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 128 : 144;
    }

    @Override
    protected int getYTexture() {
        return 32;
    }

    @Nullable
    @Override
    protected Component getHelpText() {
        return HELP;
    }
}
