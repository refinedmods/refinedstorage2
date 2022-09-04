package com.refinedmods.refinedstorage2.api.core.util;

import java.util.List;

public interface Randomizer {
    <T> T choose(List<T> list);
}
