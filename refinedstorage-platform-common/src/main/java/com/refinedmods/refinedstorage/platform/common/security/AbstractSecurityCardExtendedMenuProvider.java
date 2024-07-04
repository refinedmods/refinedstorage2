package com.refinedmods.refinedstorage.platform.common.security;

import com.refinedmods.refinedstorage.api.network.security.SecurityPolicy;
import com.refinedmods.refinedstorage.platform.api.PlatformApi;
import com.refinedmods.refinedstorage.platform.api.security.PlatformPermission;
import com.refinedmods.refinedstorage.platform.common.support.containermenu.ExtendedMenuProvider;

import java.util.List;
import java.util.Set;

abstract class AbstractSecurityCardExtendedMenuProvider<T> implements ExtendedMenuProvider<T> {
    private final SecurityPolicy securityPolicy;
    private final Set<PlatformPermission> dirtyPermissions;

    AbstractSecurityCardExtendedMenuProvider(final SecurityPolicy securityPolicy,
                                             final Set<PlatformPermission> dirtyPermissions) {
        this.securityPolicy = securityPolicy;
        this.dirtyPermissions = dirtyPermissions;
    }

    protected final List<SecurityCardData.Permission> getDataPermissions() {
        return PlatformApi.INSTANCE.getPermissionRegistry().getAll().stream().map(this::toDataPermission).toList();
    }

    private SecurityCardData.Permission toDataPermission(final PlatformPermission permission) {
        return new SecurityCardData.Permission(
            permission,
            securityPolicy.isAllowed(permission),
            dirtyPermissions.contains(permission)
        );
    }
}
