package com.refinedmods.refinedstorage.common.storage;

import com.refinedmods.refinedstorage.common.support.containermenu.ClientProperty;
import com.refinedmods.refinedstorage.common.support.widget.AbstractYesNoSideButtonWidget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createTranslation;

public class VoidExcessSideButtonWidget extends AbstractYesNoSideButtonWidget {
    private static final Component HELP = createTranslation("gui", "void_excess.help");
    private static final Component ALLOWLIST_WARNING = createTranslation(
        "gui",
        "void_excess.allowlist_warning"
    );
    private static final MutableComponent TITLE = createTranslation("gui", "void_excess");
    private static final Identifier YES = createIdentifier("widget/side_button/storage/void_excess/yes");
    private static final Identifier NO = createIdentifier("widget/side_button/storage/void_excess/no");

    public VoidExcessSideButtonWidget(final ClientProperty<Boolean> property) {
        super(property, TITLE, YES, NO);
    }

    public void setWarningVisible(final boolean visible) {
        if (visible) {
            setWarning(ALLOWLIST_WARNING);
        } else {
            setWarning(null);
        }
    }

    @Override
    @Nullable
    protected Component getHelpText() {
        return HELP;
    }
}
