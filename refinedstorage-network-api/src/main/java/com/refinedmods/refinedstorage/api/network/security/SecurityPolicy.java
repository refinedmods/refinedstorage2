package com.refinedmods.refinedstorage.api.network.security;

import java.util.Collections;
import java.util.Set;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.5")
public record SecurityPolicy(Set<Permission> allowedPermissions) {
    public static final SecurityPolicy EMPTY = new SecurityPolicy(Collections.emptySet());

    public static SecurityPolicy of(final Permission... permissions) {
        return new SecurityPolicy(Set.of(permissions));
    }

    public boolean isAllowed(final Permission permission) {
        return allowedPermissions.contains(permission);
    }
}
