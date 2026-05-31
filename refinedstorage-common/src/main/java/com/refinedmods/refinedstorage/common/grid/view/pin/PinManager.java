package com.refinedmods.refinedstorage.common.grid.view.pin;

import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PinManager {
    private final PinRepository repository;
    private final List<Pin> pins = new ArrayList<>();
    private final List<Pin> pinsView = Collections.unmodifiableList(pins);

    public PinManager(final PinRepository repository) {
        this.repository = repository;
        pins.addAll(repository.loadAll());
    }

    public void add(final int index, final GridResource gridResource) {
        if (contains(gridResource)) {
            return;
        }
        pins.add(index, new Pin(gridResource, true));
        repository.saveAll(pins);
    }

    public Pin remove(final int index) {
        final Pin removed = pins.remove(index);
        repository.saveAll(pins);
        return removed;
    }

    public List<Pin> getAll() {
        return pinsView;
    }

    public boolean contains(final GridResource gridResource) {
        for (final Pin existingPin : pins) {
            if (existingPin.is(gridResource)) {
                return true;
            }
        }
        return false;
    }
}
