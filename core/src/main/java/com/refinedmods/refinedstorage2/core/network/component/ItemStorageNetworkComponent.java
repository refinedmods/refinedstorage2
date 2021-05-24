package com.refinedmods.refinedstorage2.core.network.component;

import com.refinedmods.refinedstorage2.core.item.Rs2ItemStack;
import com.refinedmods.refinedstorage2.core.network.host.NetworkNodeHost;
import com.refinedmods.refinedstorage2.core.storage.ItemStorageChannel;
import com.refinedmods.refinedstorage2.core.storage.Storage;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemStorageNetworkComponent implements NetworkComponent {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ItemStorageChannel storageChannel = new ItemStorageChannel();
    private final List<Storage<Rs2ItemStack>> sources = new ArrayList<>();

    @Override
    public void onHostAdded(NetworkNodeHost<?> host) {
        if (host.getNode() instanceof Storage) {
            sources.add((Storage<Rs2ItemStack>) host.getNode());
            invalidate();
        }
    }

    @Override
    public void onHostRemoved(NetworkNodeHost<?> host) {
        if (host.getNode() instanceof Storage) {
            sources.remove((Storage<Rs2ItemStack>) host.getNode());
            invalidate();
        }
    }

    public void invalidate() {
        LOGGER.info("Invalidating {} storage sources", sources.size());
        storageChannel.setSources(sources);
    }

    public ItemStorageChannel getStorageChannel() {
        return storageChannel;
    }
}
