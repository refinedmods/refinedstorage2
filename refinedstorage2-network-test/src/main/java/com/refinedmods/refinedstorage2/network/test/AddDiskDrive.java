package com.refinedmods.refinedstorage2.network.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AddDiskDrive {
    long baseEnergyUsage() default 0L;

    long energyUsagePerDisk() default 0L;

    boolean active() default true;

    int diskCount() default 9;

    String networkId() default "default";
}
