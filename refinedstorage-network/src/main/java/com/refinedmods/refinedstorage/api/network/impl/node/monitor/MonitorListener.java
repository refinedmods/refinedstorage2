package com.refinedmods.refinedstorage.api.network.impl.node.monitor;

public interface MonitorListener {
    void onNodeTracked(MonitorNodeId id, MonitorNodeTypeId typeId);

    void onNodeUntracked(MonitorNodeId id);

    void onActiveChanged(boolean newActive);
}
