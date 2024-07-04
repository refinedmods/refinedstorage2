package com.refinedmods.refinedstorage.platform.api.security;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.5")
public record BuiltinPermissions(
    PlatformPermission insert,
    PlatformPermission extract,
    PlatformPermission autocrafting,
    PlatformPermission modify,
    PlatformPermission build,
    PlatformPermission security
) {
}
