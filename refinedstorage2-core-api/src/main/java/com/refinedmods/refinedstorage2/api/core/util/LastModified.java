package com.refinedmods.refinedstorage2.api.core.util;

import org.apiguardian.api.API;

@API(status = API.Status.STABLE, since = "2.0.0-milestone.1.0")
public record LastModified(Type type, long amount) {
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;
    private static final long YEAR = DAY * 365;

    public enum Type {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        WEEK,
        YEAR
    }

    public static LastModified calculate(final long time, final long now) {
        final long diff = now - time;
        final LastModified lastModified;
        if (diff < MINUTE) {
            lastModified = new LastModified(Type.SECOND, diff / SECOND);
        } else if (diff < HOUR) {
            lastModified = new LastModified(Type.MINUTE, diff / MINUTE);
        } else if (diff < DAY) {
            lastModified = new LastModified(Type.HOUR, diff / HOUR);
        } else if (diff < WEEK) {
            lastModified = new LastModified(Type.DAY, diff / DAY);
        } else if (diff < YEAR) {
            lastModified = new LastModified(Type.WEEK, diff / WEEK);
        } else {
            lastModified = new LastModified(Type.YEAR, diff / YEAR);
        }
        return lastModified;
    }
}
