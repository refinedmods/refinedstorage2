package com.refinedmods.refinedstorage2.api.grid.search;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record GridSearchBoxModeDisplayProperties(String textureIdentifier, int textureX, int textureY,
                                                 String nameTranslationKey) {
}
