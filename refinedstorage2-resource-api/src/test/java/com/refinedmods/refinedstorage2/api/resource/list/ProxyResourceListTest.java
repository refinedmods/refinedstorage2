package com.refinedmods.refinedstorage2.api.resource.list;

class ProxyResourceListTest extends AbstractResourceListTest {
    @Override
    protected ResourceList<String> createList() {
        return new AbstractProxyResourceList<>(new ResourceListImpl<>()) {
        };
    }
}
