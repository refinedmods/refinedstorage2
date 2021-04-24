package com.refinedmods.refinedstorage2.core.storage;

import java.util.Comparator;

public class PrioritizedStorageComparator implements Comparator<Storage<?>> {
    public static final Comparator<Storage<?>> INSTANCE = new PrioritizedStorageComparator();

    private static int getPriority(Storage<?> storage) {
        if (storage instanceof Priority) {
            return ((Priority) storage).getPriority();
        }
        return 0;
    }

    @Override
    public int compare(Storage<?> o1, Storage<?> o2) {
        return Integer.compare(getPriority(o2), getPriority(o1));
    }
}
