package com.refinedmods.refinedstorage2.platform.api.upgrade;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.2")
public record UpgradeInDestination(UpgradeDestination destination,
                                   int maxAmount) {
}
