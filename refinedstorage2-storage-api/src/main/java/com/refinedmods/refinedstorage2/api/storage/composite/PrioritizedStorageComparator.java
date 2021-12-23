package com.refinedmods.refinedstorage2.api.storage.composite;

import com.refinedmods.refinedstorage2.api.storage.Storage;

import java.util.Comparator;

class PrioritizedStorageComparator implements Comparator<Storage<?>> {
    static final Comparator<Storage<?>> INSTANCE = new PrioritizedStorageComparator();

    private static int getPriority(Storage<?> storage) {
        if (storage instanceof Priority priority) {
            return priority.getPriority();
        }
        return 0;
    }

    @Override
    public int compare(Storage<?> o1, Storage<?> o2) {
        return Integer.compare(getPriority(o2), getPriority(o1));
    }
}
