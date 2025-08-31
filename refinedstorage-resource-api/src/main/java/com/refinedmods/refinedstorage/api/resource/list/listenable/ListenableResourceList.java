package com.refinedmods.refinedstorage.api.resource.list.listenable;

import com.refinedmods.refinedstorage.api.resource.ResourceKey;
import com.refinedmods.refinedstorage.api.resource.list.AbstractProxyMutableResourceList;
import com.refinedmods.refinedstorage.api.resource.list.MutableResourceList;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource list that can have listeners to track changes.
 * Can easily be used with an existing list by passing it in the constructor.
 * The {@link ResourceListListener#changed(OperationResult)} method is only called when the change
 * is being performed through this list, not the delegate list.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.2")
public class ListenableResourceList extends AbstractProxyMutableResourceList {
    private static final Logger LOGGER = LoggerFactory.getLogger(ListenableResourceList.class);
    private final Set<ResourceListListener> listeners = new HashSet<>();

    public ListenableResourceList(final MutableResourceList delegate) {
        super(delegate);
    }

    @Override
    public OperationResult add(final ResourceKey resource, final long amount) {
        final OperationResult result = super.add(resource, amount);
        notifyListeners(result);
        return result;
    }

    @Override
    @Nullable
    public OperationResult remove(final ResourceKey resource, final long amount) {
        final OperationResult result = super.remove(resource, amount);
        if (result != null) {
            notifyListeners(result);
        }
        return result;
    }

    private void notifyListeners(final OperationResult result) {
        LOGGER.info("Notifying {} listeners of change: {}", listeners.size(), result);
        listeners.forEach(listener -> listener.changed(result));
    }

    public void addListener(final ResourceListListener listener) {
        LOGGER.info("Adding listener: {}", listener);
        listeners.add(listener);
    }

    public void removeListener(final ResourceListListener listener) {
        LOGGER.info("Removing listener: {}", listener);
        listeners.remove(listener);
    }
}
