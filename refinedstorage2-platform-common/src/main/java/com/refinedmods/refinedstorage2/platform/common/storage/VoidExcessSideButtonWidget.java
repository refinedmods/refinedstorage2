package com.refinedmods.refinedstorage2.platform.common.storage;

import com.refinedmods.refinedstorage2.platform.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage2.platform.common.support.widget.AbstractYesNoSideButtonWidget;

import javax.annotation.Nullable;

import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage2.platform.common.util.IdentifierUtil.createTranslation;

public class VoidExcessSideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final Component HELP = createTranslation("gui", "void_excess.help");
    private static final Component ALLOWLIST_WARNING = createTranslation(
        "gui",
        "void_excess.allowlist_warning"
    );

    public VoidExcessSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, createTranslation("gui", "void_excess"));
    }

    public void setWarningVisible(final boolean visible) {
        if (visible) {
            setWarning(ALLOWLIST_WARNING);
        } else {
            setWarning(null);
        }
    }

    @Override
    protected int getXTexture() {
        return Boolean.TRUE.equals(property.getValue()) ? 16 : 0;
    }

    @Override
    protected int getYTexture() {
        return 240 - 15;
    }

    @Override
    @Nullable
    protected Component getHelpText() {
        return HELP;
    }
}
