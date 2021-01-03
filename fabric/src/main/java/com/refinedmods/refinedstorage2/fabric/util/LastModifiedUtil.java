package com.refinedmods.refinedstorage2.fabric.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;

public class LastModifiedUtil {
    private static final long SECOND = 1000;
    private static final long MINUTE = SECOND * 60;
    private static final long HOUR = MINUTE * 60;
    private static final long DAY = HOUR * 24;
    private static final long WEEK = DAY * 7;
    private static final long YEAR = DAY * 365;

    private LastModifiedUtil() {
    }

    public static MutableText getText(long time, String name) {
        long diff = System.currentTimeMillis() - time;

        if (diff < SECOND * 10) {
            return new TranslatableText("misc.refinedstorage2.last_modified.just_now", name);
        } else if (diff < MINUTE) {
            return new TranslatableText("misc.refinedstorage2.last_modified.second" + ((diff / SECOND) > 1 ? "s" : ""), diff / SECOND, name);
        } else if (diff < HOUR) {
            return new TranslatableText("misc.refinedstorage2.last_modified.minute" + ((diff / MINUTE) > 1 ? "s" : ""), diff / MINUTE, name);
        } else if (diff < DAY) {
            return new TranslatableText("misc.refinedstorage2.last_modified.hour" + ((diff / HOUR) > 1 ? "s" : ""), diff / HOUR, name);
        } else if (diff < WEEK) {
            return new TranslatableText("misc.refinedstorage2.last_modified.day" + ((diff / DAY) > 1 ? "s" : ""), diff / DAY, name);
        } else if (diff < YEAR) {
            return new TranslatableText("misc.refinedstorage2.last_modified.week" + ((diff / WEEK) > 1 ? "s" : ""), diff / WEEK, name);
        }

        return new TranslatableText("misc.refinedstorage2.last_modified.year" + ((diff / YEAR) > 1 ? "s" : ""), diff / YEAR, name);
    }
}

