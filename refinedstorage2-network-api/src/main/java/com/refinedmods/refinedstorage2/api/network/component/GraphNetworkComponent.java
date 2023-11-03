package com.refinedmods.refinedstorage2.api.network.component;

import com.refinedmods.refinedstorage2.api.network.node.container.NetworkNodeContainer;

import java.util.Set;
import javax.annotation.Nullable;

import org.apiguardian.api.API;

/**
 * Responsible for managing the connected network nodes in a network.
 */
@API(status = API.Status.STABLE, since = "2.0.0-milestone.3.2")
public interface GraphNetworkComponent extends NetworkComponent {
    /**
     * @return all the containers currently present in this graph
     */
    Set<NetworkNodeContainer> getContainers();

    /**
     * Retrieves containers by container type.
     * It can be queried by exact class type or by interface type.
     *
     * @param clazz the container class type
     * @param <T>   the container class type
     * @return the containers matching the type
     */
    <T> Set<T> getContainers(Class<T> clazz);

    /**
     * Retrieves a container by key, defined in {@link NetworkNodeContainer#createKey()}.
     *
     * @param key the key
     * @return the container, or null if not found
     */
    @Nullable
    NetworkNodeContainer getContainer(Object key);
}
