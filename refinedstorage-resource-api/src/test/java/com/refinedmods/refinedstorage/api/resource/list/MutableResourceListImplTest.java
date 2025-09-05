package com.refinedmods.refinedstorage.api.resource.list;

import com.refinedmods.refinedstorage.api.resource.TestResource;

class MutableResourceListImplTest extends AbstractMutableResourceListTest {
    @Override
    protected MutableResourceList createList(final TestResource[] resources, final long amount) {
        final MutableResourceListImpl mutableResourceList = MutableResourceListImpl.create();
        for (final TestResource resource : resources) {
            mutableResourceList.add(resource, amount);
        }
        return mutableResourceList;
    }
}
