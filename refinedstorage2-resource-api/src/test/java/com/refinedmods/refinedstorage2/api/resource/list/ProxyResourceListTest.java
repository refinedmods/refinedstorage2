package com.refinedmods.refinedstorage2.api.resource.list;

class ProxyResourceListTest extends AbstractResourceListTest {
    @Override
    protected ResourceList createList() {
        return new AbstractProxyResourceList(new ResourceListImpl()) {
        };
    }
}
