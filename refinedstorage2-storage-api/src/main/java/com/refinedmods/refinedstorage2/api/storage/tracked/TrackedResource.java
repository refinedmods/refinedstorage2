package com.refinedmods.refinedstorage2.api.storage.tracked;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.4")
public final class TrackedResource {
    private String sourceName;
    private long time;

    public TrackedResource(final String sourceName, final long time) {
        this.sourceName = sourceName;
        this.update(sourceName, time);
    }

    public void update(final String newSourceName, final long newTime) {
        this.sourceName = newSourceName;
        this.time = newTime;
    }

    public String getSourceName() {
        return sourceName;
    }

    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "TrackedResource{"
            + "sourceName='" + sourceName + '\''
            + ", time=" + time
            + '}';
    }
}
