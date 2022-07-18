package com.refinedmods.refinedstorage2.api.network.node.importer;

import com.refinedmods.refinedstorage2.api.core.filter.Filter;
import com.refinedmods.refinedstorage2.api.storage.Actor;

public interface ImporterTransferStrategy {
    boolean transfer(Filter filter, Actor actor);
}
