package com.refinedmods.refinedstorage.platform.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

import static com.refinedmods.refinedstorage.platform.common.util.IdentifierUtil.createTranslation;

public final class SecurityToastUtil {
    private static final SystemToast.SystemToastId NO_PERMISSION_TOAST_ID = new SystemToast.SystemToastId();

    private SecurityToastUtil() {
    }

    public static void addNoPermissionToast(final Component message) {
        SystemToast.add(
            Minecraft.getInstance().getToasts(),
            NO_PERMISSION_TOAST_ID,
            createTranslation("misc", "no_permission"),
            message
        );
    }
}
