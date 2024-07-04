package com.refinedmods.refinedstorage.platform.api.support.resource;

import java.util.Optional;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.5")
public interface RecipeModIngredientConverter {
    Optional<PlatformResourceKey> convertToResource(Object ingredient);

    Optional<Object> convertToIngredient(PlatformResourceKey resource);
}
