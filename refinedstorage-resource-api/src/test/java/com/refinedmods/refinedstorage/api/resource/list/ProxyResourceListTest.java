package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.TestResource;

class ProxyResourceListTest extends AbstractMutableResourceListTest {
    @Override
    protected MutableResourceList createList(final TestResource[] resources, final long amount) {
        final MutableResourceListImpl delegate = MutableResourceListImpl.create();
        for (final TestResource resource : resources) {
            delegate.add(resource, amount);
        }
        return new AbstractProxyMutableResourceList(delegate) {
        };
    }
}
