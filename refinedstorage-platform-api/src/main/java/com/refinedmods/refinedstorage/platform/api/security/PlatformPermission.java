package com.refinedmods.refinedstorage.platform.api.security;

import com.refinedmods.refinedstorage.api.network.security.Permission;

import net.minecraft.network.chat.Component;
import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.5")
public interface PlatformPermission extends Permission {
    /**
     * @return the permission name
     */
    Component getName();

    /**
     * @return a short description of the permission
     */
    Component getDescription();

    /**
     * @return the name of the mod that adds this permission
     */
    Component getOwnerName();

    /**
     * Determines whether this permission is allowed by default, when it is not configured (yet)
     * in a Security Card.
     *
     * @return true if this permission is allowed by default, false otherwise
     */
    boolean isAllowedByDefault();
}
