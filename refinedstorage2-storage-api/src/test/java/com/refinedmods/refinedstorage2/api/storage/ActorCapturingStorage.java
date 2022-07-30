package com.refinedmods.refinedstorage2.api.storage;

import com.refinedmods.refinedstorage2.api.core.Action;

import java.util.ArrayList;
import java.util.List;

public class ActorCapturingStorage<T> extends AbstractProxyStorage<T> {
    private final List<Actor> actors = new ArrayList<>();

    public ActorCapturingStorage(final Storage<T> delegate) {
        super(delegate);
    }

    @Override
    public long extract(final T resource, final long amount, final Action action, final Actor actor) {
        actors.add(actor);
        return super.extract(resource, amount, action, actor);
    }

    @Override
    public long insert(final T resource, final long amount, final Action action, final Actor actor) {
        actors.add(actor);
        return super.insert(resource, amount, action, actor);
    }

    public List<Actor> getActors() {
        return actors;
    }
}
