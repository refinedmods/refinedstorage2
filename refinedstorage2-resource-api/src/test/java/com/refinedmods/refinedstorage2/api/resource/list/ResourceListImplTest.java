package com.refinedmods.refinedstorage2.api.resource.list;

class ResourceListImplTest extends AbstractResourceListTest {
    @Override
    protected ResourceList<String> createList() {
        return new ResourceListImpl<>();
    }
}
