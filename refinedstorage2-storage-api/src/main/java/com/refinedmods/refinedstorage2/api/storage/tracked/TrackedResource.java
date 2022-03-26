package com.refinedmods.refinedstorage2.api.storage.tracked;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public final class TrackedResource {
    private String sourceName;
    private long time;

    public TrackedResource(String sourceName, long time) {
        update(sourceName, time);
    }

    public void update(String sourceName, long time) {
        this.sourceName = sourceName;
        this.time = time;
    }

    public String getSourceName() {
        return sourceName;
    }

    public long getTime() {
        return time;
    }
}
