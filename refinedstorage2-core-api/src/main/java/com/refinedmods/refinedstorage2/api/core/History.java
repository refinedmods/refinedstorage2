package com.refinedmods.refinedstorage2.api.core;

import java.util.List;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public class History {
    private final List<String> items;
    private int index = -1;

    public History(final List<String> items) {
        this.items = items;
    }

    public String older() {
        if (items.isEmpty()) {
            return "";
        }

        if (index == -1) {
            index = items.size() - 1;
            return items.get(index);
        } else {
            index--;

            if (index < 0) {
                index = 0;
            }

            return items.get(index);
        }
    }

    public String newer() {
        if (index == -1) {
            return "";
        }

        index++;

        if (index == items.size()) {
            index = -1;
            return "";
        }

        return items.get(index);
    }

    public boolean save(final String value) {
        if (value.trim().isEmpty()) {
            return false;
        }

        if (!items.isEmpty() && items.get(items.size() - 1).equals(value)) {
            return false;
        }

        items.add(value);
        return true;
    }
}
