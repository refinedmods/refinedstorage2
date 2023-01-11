package com.refinedmods.refinedstorage2.api.core.util;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.2.4")
public interface Randomizer {
    <T> void shuffle(List<T> list);
}
