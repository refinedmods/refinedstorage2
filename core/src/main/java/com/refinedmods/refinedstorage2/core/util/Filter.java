package com.refinedmods.refinedstorage2.core.util;

import com.refinedmods.refinedstorage2.api.stack.list.StackList;

import java.util.List;

public abstract class Filter<T> {
    private boolean exact = true;
    private StackList<T> list = createList(true);
    private FilterMode mode = FilterMode.BLOCK;

    protected abstract StackList<T> createList(boolean exact);

    public FilterMode getMode() {
        return mode;
    }

    public void setMode(FilterMode mode) {
        this.mode = mode;
    }

    public boolean isExact() {
        return exact;
    }

    public void setExact(boolean exact) {
        this.exact = exact;

        StackList<T> oldList = list;
        this.list = createList(exact);
        oldList.getAll().forEach(t -> this.list.add(t, 1));
    }

    public boolean isAllowed(T template) {
        return switch (mode) {
            case ALLOW -> list.get(template).isPresent();
            case BLOCK -> list.get(template).isEmpty();
        };
    }

    public void setTemplates(List<T> templates) {
        list.clear();
        templates.forEach(t -> list.add(t, 1));
    }
}
