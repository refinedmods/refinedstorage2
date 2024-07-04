package com.refinedmods.refinedstorage.platform.api;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.13")
public interface RefinedStoragePlugin {
    void onPlatformApiAvailable(PlatformApi platformApi);
}
