package com.refinedmods.refinedstorage2.api.core.component;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public interface ComponentAccessor<C> {
    <C2 extends C> C2 getComponent(Class<C2> componentType);
}
