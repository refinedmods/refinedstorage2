package com.refinedmods.refinedstorage.common.grid.view.pin;

import com.refinedmods.refinedstorage.api.autocrafting.task.TaskId;
import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.repository.ResourceRepositoryMapper;
import com.refinedmods.refinedstorage.common.api.grid.view.GridResource;
import com.refinedmods.refinedstorage.common.api.support.resource.PlatformResourceKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PinManager {
    private final PinRepository repository;
    private final ResourceRepositoryMapper<GridResource> mapper;
    private final List<Pin> pins = new ArrayList<>();
    private final List<Pin> pinsView = Collections.unmodifiableList(pins);
    private final Map<ResourceKey, Set<TaskId>> currentlyAutocrafting = new HashMap<>();
    private final Set<ResourceKey> resourcesToPurge = new HashSet<>();

    public PinManager(final PinRepository repository, final ResourceRepositoryMapper<GridResource> mapper) {
        this.repository = repository;
        pins.addAll(repository.loadAll());
        this.mapper = mapper;
    }

    public void add(final int index, final GridResource gridResource) {
        if (contains(gridResource)) {
            return;
        }
        pins.add(index, new Pin(gridResource, true));
        repository.saveAll(pins);
    }

    public void loadAutocrafting(final Map<PlatformResourceKey, Set<TaskId>> current) {
        purgeAutocraftingPins();
        final Set<ResourceKey> remainingResources = new HashSet<>(currentlyAutocrafting.keySet());
        currentlyAutocrafting.clear();
        currentlyAutocrafting.putAll(current);
        remainingResources.removeAll(current.keySet());
        resourcesToPurge.addAll(remainingResources);
        currentlyAutocrafting.keySet().forEach(this::tryAddAutocraftingPin);
    }

    private void purgeAutocraftingPins() {
        pins.removeIf(pin -> !pin.manual() && resourcesToPurge.contains(pin.gridResource().getAutocraftingResource()));
        resourcesToPurge.clear();
    }

    private void tryAddAutocraftingPin(final ResourceKey resource) {
        if (contains(resource)) {
            return;
        }
        final GridResource gridResource = mapper.apply(resource);
        pins.add(new Pin(gridResource, false));
    }

    public Pin remove(final int index) {
        final Pin removed = pins.remove(index);
        repository.saveAll(pins);
        return removed;
    }

    public List<Pin> getAll() {
        return pinsView;
    }

    public Set<TaskId> getAutocraftingTasks(final ResourceKey resource) {
        return currentlyAutocrafting.getOrDefault(resource, Collections.emptySet());
    }

    public boolean contains(final GridResource gridResource) {
        return pins.stream().anyMatch(pin -> pin.is(gridResource));
    }

    private boolean contains(final ResourceKey resource) {
        return pins.stream().anyMatch(pin -> Objects.equals(pin.gridResource().getAutocraftingResource(), resource));
    }
}
