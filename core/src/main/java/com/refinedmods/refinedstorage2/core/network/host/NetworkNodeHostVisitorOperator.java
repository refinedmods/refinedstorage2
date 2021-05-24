package com.refinedmods.refinedstorage2.core.network.host;

import com.refinedmods.refinedstorage2.core.Rs2World;
import com.refinedmods.refinedstorage2.core.util.Position;

public interface NetworkNodeHostVisitorOperator {
    void apply(Rs2World world, Position position);
}
