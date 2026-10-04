package com.refinedmods.refinedstorage.network.test;

import com.refinedmods.refinedstorage.api.network.node.NetworkNodeListener;

import java.util.ArrayList;
import java.util.List;

public class RecordingNetworkNodeListener implements NetworkNodeListener {
    public final List<Object> events = new ArrayList<>();

    @Override
    public void notify(final Object event) {
        events.add(event);
    }
}
